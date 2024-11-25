package com.example.pingpong.pong.controller;

import com.example.pingpong.pong.model.PongResponse;
import com.example.pingpong.pong.service.PongService;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/pong")
public class PongController {
    private static final Logger log = LoggerFactory.getLogger(PongController.class);
    private final PongService pongService;
    private final RateLimiter rateLimiter;

    public PongController(PongService pongService, RateLimiter rateLimiter) {
        this.pongService = pongService;
        this.rateLimiter = rateLimiter;
    }

    @GetMapping("/{message}")
    public Mono<ResponseEntity<PongResponse>> handlePing(
            @PathVariable("message") String message) {
        
        return Mono.just(message)
                .transform(RateLimiterOperator.of(rateLimiter))
                .flatMap(msg -> pongService.createPongResponse(msg))
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Request processed successfully"))
                .doOnError(error -> log.error("Error processing request", error))
                .onErrorResume(RequestNotPermitted.class, e -> {
                    PongResponse response = PongResponse.builder()
                            .message("Rate limit exceeded")
                            .status("RATE_LIMITED")
                            .responseTime(LocalDateTime.now())
                            .pongInstanceId(pongService.getInstanceId())
                            .build();
                    return Mono.just(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response));
                });
    }
}
