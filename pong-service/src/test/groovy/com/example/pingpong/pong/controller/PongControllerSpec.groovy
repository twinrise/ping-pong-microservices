package com.example.pingpong.pong.controller

import com.example.pingpong.pong.model.PongResponse
import com.example.pingpong.pong.service.PongService
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration
import java.time.LocalDateTime

class PongControllerSpec extends Specification {

    PongService pongService
    RateLimiter rateLimiter
    PongController controller

    def setup() {
        pongService = Mock(PongService)
        def config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(1)
            .timeoutDuration(Duration.ofMillis(500))  
            .writableStackTraceEnabled(true)
            .build()
            
        rateLimiter = RateLimiterRegistry.of(config).rateLimiter("test")
        controller = new PongController(pongService, rateLimiter)
    }

    def "should handle ping request successfully"() {
        given:
        def message = "hello"
        def pongResponse = PongResponse.builder()
            .message("World!")
            .status("SUCCESS")
            .pongInstanceId("test-instance")
            .responseTime(LocalDateTime.now())
            .build()
        pongService.createPongResponse(message) >> Mono.just(pongResponse)

        when:
        def result = controller.handlePing(message)

        then:
        StepVerifier.create(result)
                .expectNextMatches { ResponseEntity response ->
                    response.statusCode == HttpStatus.OK &&
                    response.body.status == "SUCCESS" &&
                    response.body.message == "World!"
                }
                .verifyComplete()
    }

    def "should verify rate limiter behavior"() {
        given: "a mock response"
        def message = "hello"
        def pongResponse = PongResponse.builder()
            .message("World!")
            .status("SUCCESS")
            .pongInstanceId("test-instance")
            .responseTime(LocalDateTime.now())
            .build()
        pongService.createPongResponse(message) >> Mono.just(pongResponse)
        pongService.getInstanceId() >> "test-instance"

        expect: "first request should succeed"
        StepVerifier.create(controller.handlePing(message))
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK
            }
            .verifyComplete()

        and: "second request should be rejected immediately"
        def result = controller.handlePing(message).block()
        result.statusCode == HttpStatus.TOO_MANY_REQUESTS
        result.body.message == "Rate limit exceeded"
        result.body.status == "RATE_LIMITED"
        result.body.responseTime != null
        result.body.pongInstanceId == "test-instance"
        
        and: "after waiting, third request should succeed"
        Thread.sleep(1100)  // 等待速率限制重置
        StepVerifier.create(controller.handlePing(message))
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK
            }
            .verifyComplete()
    }

    def "should handle concurrent requests correctly"() {
        given: "a mock response"
        def message = "hello"
        def pongResponse = PongResponse.builder()
            .message("World!")
            .status("SUCCESS")
            .pongInstanceId("test-instance")
            .responseTime(LocalDateTime.now())
            .build()
        pongService.createPongResponse(message) >> Mono.just(pongResponse)
        pongService.getInstanceId() >> "test-instance"
        
        when: "sending multiple requests rapidly"
        def startTime = System.currentTimeMillis()
        def results = []
        5.times {
            results << controller.handlePing(message).block()
        }

        then: "only one request should succeed"
        results.count { it.statusCode == HttpStatus.OK } == 1
        results.count { it.statusCode == HttpStatus.TOO_MANY_REQUESTS } == 4
        results.findAll { it.statusCode == HttpStatus.TOO_MANY_REQUESTS }.every {
            it.body.message == "Rate limit exceeded" &&
            it.body.status == "RATE_LIMITED" &&
            it.body.responseTime != null &&
            it.body.pongInstanceId == "test-instance"
        }

        and: "requests should complete quickly"
        def timeElapsed = System.currentTimeMillis() - startTime
        timeElapsed < 100  // 验证所有请求是否快速完成
    }
}
