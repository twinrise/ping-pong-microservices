package com.example.pingpong.pong.controller

import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import org.springframework.http.HttpStatus
import spock.lang.Specification

import java.time.Duration

class GlobalExceptionHandlerSpec extends Specification {
    GlobalExceptionHandler handler
    RateLimiter rateLimiter

    def setup() {
        handler = new GlobalExceptionHandler()
        def config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(1)
            .build()
        rateLimiter = RateLimiter.of("test", config)
    }

    def "should handle RequestNotPermitted exception"() {
        given:
        def exception = RequestNotPermitted.createRequestNotPermitted(rateLimiter)

        when:
        def response = handler.handleRequestNotPermitted(exception)

        then:
        response.statusCode == HttpStatus.TOO_MANY_REQUESTS
        response.body.message == "Rate limit exceeded"
        response.body.status == "RATE_LIMITED"
    }

    def "should include appropriate response for rate limit"() {
        given:
        def exception = RequestNotPermitted.createRequestNotPermitted(rateLimiter)

        when:
        def response = handler.handleRequestNotPermitted(exception)
        def body = response.body

        then:
        body.message != null
        body.status != null
        body.responseTime != null
    }

    def "should maintain consistent response structure"() {
        given:
        def rateLimiter1 = RateLimiter.of("test1", RateLimiterConfig.ofDefaults())
        def rateLimiter2 = RateLimiter.of("test2", RateLimiterConfig.ofDefaults())
        def exception1 = RequestNotPermitted.createRequestNotPermitted(rateLimiter1)
        def exception2 = RequestNotPermitted.createRequestNotPermitted(rateLimiter2)

        when:
        def response1 = handler.handleRequestNotPermitted(exception1)
        def response2 = handler.handleRequestNotPermitted(exception2)

        then:
        response1.statusCode == response2.statusCode
        response1.body.status == response2.body.status
        response1.body.message == response2.body.message
    }
}
