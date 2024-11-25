package com.example.pingpong.pong.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class RateLimitService {
    private final RateLimiter rateLimiter;

    public RateLimitService(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    public Mono<Boolean> tryRequest() {
        return Mono.just(true)
            .transformDeferred(RateLimiterOperator.of(rateLimiter))
            .map(result -> true)
            .onErrorReturn(RequestNotPermitted.class, false);
    }
}
