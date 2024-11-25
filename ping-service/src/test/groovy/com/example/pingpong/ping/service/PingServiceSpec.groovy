package com.example.pingpong.ping.service

import com.example.pingpong.ping.ratelimit.HazelcastRateLimiter
import com.example.pingpong.shared.message.PingLogMessage
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.util.concurrent.TimeoutException

class PingServiceSpec extends Specification {
    @Subject
    PingService pingService

    WebClient.Builder webClientBuilder = Mock()
    WebClient webClient = Mock()
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec = Mock()
    WebClient.RequestHeadersSpec requestHeadersSpec = Mock()
    WebClient.ResponseSpec responseSpec = Mock()
    RocketMQTemplate rocketMQTemplate = Mock()
    HazelcastRateLimiter rateLimiter = Mock()

    def setup() {
        webClientBuilder.build() >> webClient
        
        pingService = new PingService(webClientBuilder, rocketMQTemplate, rateLimiter)
        
        // 设置必要的配置属性
        ReflectionTestUtils.setField(pingService, "serviceName", "ping-service")
        ReflectionTestUtils.setField(pingService, "pongServiceBaseUrl", "http://localhost:8081")
        ReflectionTestUtils.setField(pingService, "pongEndpoint", "/pong")
        ReflectionTestUtils.setField(pingService, "maxAttempts", 3)
        ReflectionTestUtils.setField(pingService, "initialBackoff", 1000L)
    }

    def "should successfully send ping and receive response"() {
        given:
        def expectedResponse = "Hello from Pong!"
        rateLimiter.tryAcquire() >> true
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        requestHeadersSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> Mono.just(expectedResponse)

        when:
        def result = pingService.sendPing()
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete()

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(String.class) >> Mono.just(expectedResponse)
        1 * rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", { PingLogMessage msg ->
            msg.status == "已发送" &&
            msg.response == expectedResponse &&
            msg.serviceName == "ping-service"
        })
    }

    def "should handle rate limit exceeded"() {
        given: "rate limiter denies the request"
        rateLimiter.tryAcquire() >> false

        and: "webclient is not configured because rate limit will prevent its use"
        0 * webClient.get()
        0 * requestHeadersUriSpec.uri(_)
        0 * requestHeadersSpec.retrieve()
        0 * responseSpec.bodyToMono(_)

        when: "sending a ping request"
        def result = pingService.sendPing()

        then: "verify the error response"
        StepVerifier.create(result)
            .expectErrorMatches({ error ->
                error instanceof RuntimeException &&
                error.message == "Rate limit exceeded"
            })
            .verify()

        and: "verify the error is logged"
        1 * rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", { PingLogMessage msg ->
            msg.status == "未发送" &&
            msg.errorMessage == "Client Rate limit exceeded" &&
            msg.serviceName == "ping-service"
        })
    }

    def "should handle web client error"() {
        given:
        def errorMessage = "Connection refused"
        rateLimiter.tryAcquire() >> true
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        requestHeadersSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> Mono.error(new RuntimeException(errorMessage))

        when:
        def result = pingService.sendPing()
        StepVerifier.create(result)
            .expectErrorMatches({ error ->
                error instanceof RuntimeException &&
                error.message == errorMessage
            })
            .verify()

        then:
        1 * webClient.get() >> requestHeadersUriSpec
        1 * requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        1 * requestHeadersSpec.retrieve() >> responseSpec
        1 * responseSpec.bodyToMono(String.class) >> Mono.error(new RuntimeException(errorMessage))
        1 * rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", { PingLogMessage msg ->
            msg.status == "未发送" &&
            msg.errorMessage == errorMessage &&
            msg.serviceName == "ping-service"
        })
    }

    def "should handle timeout"() {
        given:
        rateLimiter.tryAcquire() >> true
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        requestHeadersSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> Mono.never().timeout(Duration.ofSeconds(1))

        when:
        def result = pingService.sendPing()
        StepVerifier.create(result)
            .expectErrorMatches({ error ->
                error instanceof TimeoutException &&
                error.message.contains("Did not observe any item or terminal signal within")
            })
            .verify()

        then:
        1 * rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", { PingLogMessage msg ->
            msg.status == "未发送" &&
            msg.errorMessage.contains("Did not observe any item or terminal signal within") &&
            msg.serviceName == "ping-service"
        })
    }


    def "should set correct processId and serviceId"() {
        given: "a successful ping request setup"
        def expectedResponse = "Hello from Pong!"
        def expectedProcessId = String.valueOf(ProcessHandle.current().pid())
        rateLimiter.tryAcquire() >> true
        webClient.get() >> requestHeadersUriSpec
        requestHeadersUriSpec.uri(_) >> requestHeadersSpec
        requestHeadersSpec.retrieve() >> responseSpec
        responseSpec.bodyToMono(String.class) >> Mono.just(expectedResponse)

        when: "sending a ping request"
        def result = pingService.sendPing()
        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete()

        then: "verify processId and serviceId in log message"
        1 * rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", { PingLogMessage msg ->
            msg.processId == expectedProcessId &&
            msg.serviceName == "ping-service"
        })
    }
}
