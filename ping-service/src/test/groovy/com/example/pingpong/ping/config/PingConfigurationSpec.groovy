package com.example.pingpong.ping.config

import com.example.pingpong.ping.ratelimit.HazelcastRateLimiter
import com.example.pingpong.ping.service.PingService
import org.apache.rocketmq.spring.core.RocketMQTemplate
import org.springframework.web.reactive.function.client.WebClient
import spock.lang.Specification
import spock.lang.Subject

class PingConfigurationSpec extends Specification {
    @Subject
    PingConfiguration pingConfiguration

    def setup() {
        pingConfiguration = new PingConfiguration()
    }

    def "should create WebClient.Builder"() {
        when: "creating WebClient.Builder"
        def builder = pingConfiguration.webClientBuilder()

        then: "builder should be created"
        builder instanceof WebClient.Builder
    }

    def "should create PingService with all dependencies"() {
        given: "all required dependencies"
        def webClientBuilder = Mock(WebClient.Builder)
        def rocketMQTemplate = Mock(RocketMQTemplate)
        def rateLimiter = Mock(HazelcastRateLimiter)

        when: "creating PingService"
        def pingService = pingConfiguration.pingService(webClientBuilder, rocketMQTemplate, rateLimiter)

        then: "PingService should be created with all dependencies"
        pingService instanceof PingService
        noExceptionThrown()
    }
}
