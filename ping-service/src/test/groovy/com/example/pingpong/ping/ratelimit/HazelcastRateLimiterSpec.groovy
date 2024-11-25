package com.example.pingpong.ping.ratelimit

import com.hazelcast.core.HazelcastInstance
import com.hazelcast.map.IMap
import spock.lang.Specification
import spock.lang.Subject

import java.time.Duration
import java.time.Instant

class HazelcastRateLimiterSpec extends Specification {
    static final String MAP_NAME = "rate-limit-map"
    static final String COUNTER_KEY = "request-counter"
    static final int MAX_REQUESTS = 10
    static final Duration WINDOW_DURATION = Duration.ofSeconds(1)

    @Subject
    HazelcastRateLimiter rateLimiter

    HazelcastInstance hazelcastInstance = Mock()
    IMap<String, HazelcastRateLimiter.RateLimitEntry> rateMap = Mock()

    def setup() {
        hazelcastInstance.getMap(MAP_NAME) >> rateMap
        rateLimiter = new HazelcastRateLimiter(hazelcastInstance, MAX_REQUESTS, WINDOW_DURATION.toSeconds() as int)
    }

    def "should allow first request"() {
        given: "no previous requests"
        rateMap.get(COUNTER_KEY) >> null

        when:
        def result = rateLimiter.tryAcquire()

        then:
        result == true
        1 * rateMap.put(COUNTER_KEY, { HazelcastRateLimiter.RateLimitEntry entry ->
            entry.second == Instant.now().epochSecond &&
            entry.count == 1
        })
    }

    def "should allow request within limit"() {
        given: "existing requests within limit"
        def currentSecond = Instant.now().epochSecond
        def currentCount = 5
        rateMap.get(COUNTER_KEY) >> new HazelcastRateLimiter.RateLimitEntry(currentSecond, currentCount)

        when:
        def result = rateLimiter.tryAcquire()

        then:
        result == true
        1 * rateMap.put(COUNTER_KEY, { HazelcastRateLimiter.RateLimitEntry entry ->
            entry.second == currentSecond &&
            entry.count == currentCount + 1
        })
    }

    def "should deny request when limit exceeded"() {
        given: "existing requests at limit"
        def currentSecond = Instant.now().epochSecond
        rateMap.get(COUNTER_KEY) >> new HazelcastRateLimiter.RateLimitEntry(currentSecond, MAX_REQUESTS)

        when:
        def result = rateLimiter.tryAcquire()

        then:
        result == false
        0 * rateMap.put(_, _)
    }

    def "should reset counter for new time window"() {
        given: "old requests from previous second"
        def currentSecond = Instant.now().epochSecond
        rateMap.get(COUNTER_KEY) >> new HazelcastRateLimiter.RateLimitEntry(currentSecond - 1, MAX_REQUESTS)

        when:
        def result = rateLimiter.tryAcquire()

        then:
        result == true
        1 * rateMap.put(COUNTER_KEY, { HazelcastRateLimiter.RateLimitEntry entry ->
            entry.second == currentSecond &&
            entry.count == 1
        })
    }

    def "should handle map operation exceptions"() {
        given: "map throws exception"
        rateMap.get(COUNTER_KEY) >> { throw new RuntimeException("Map error") }

        when:
        def result = rateLimiter.tryAcquire()

        then:
        thrown(RuntimeException)
        0 * rateMap.put(_, _)
    }
}