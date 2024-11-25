package com.example.pingpong.pong.service

import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import reactor.test.StepVerifier
import spock.lang.Specification

import java.time.Duration

class RateLimitServiceSpec extends Specification {
    RateLimitService rateLimitService
    RateLimiter rateLimiter

    def setup() {
        def config = RateLimiterConfig.custom()
            .limitRefreshPeriod(Duration.ofSeconds(1))
            .limitForPeriod(1)
            .timeoutDuration(Duration.ZERO)
            .build()
        def registry = RateLimiterRegistry.of(config)
        rateLimiter = registry.rateLimiter("test")
        rateLimitService = new RateLimitService(rateLimiter)
    }

    def "should allow first request within limit"() {
        expect:
        StepVerifier.create(rateLimitService.tryRequest())
            .expectNext(true)
            .verifyComplete()
    }

    def "should reject request when rate limit exceeded"() {
        given: "first request consumes the limit"
        StepVerifier.create(rateLimitService.tryRequest())
            .expectNext(true)
            .verifyComplete()

        expect: "second request is rejected"
        StepVerifier.create(rateLimitService.tryRequest())
            .expectNext(false)
            .verifyComplete()
    }

    def "should allow request after refresh period"() {
        given: "first request consumes the limit"
        StepVerifier.create(rateLimitService.tryRequest())
            .expectNext(true)
            .verifyComplete()

        and: "wait for refresh period"
        Thread.sleep(1100)

        expect: "new request is allowed"
        StepVerifier.create(rateLimitService.tryRequest())
            .expectNext(true)
            .verifyComplete()
    }
}
