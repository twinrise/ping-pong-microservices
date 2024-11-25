package com.example.pingpong.pong.controller;

import com.example.pingpong.pong.model.PongResponse;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<PongResponse> handleRequestNotPermitted(RequestNotPermitted e) {
        PongResponse response = PongResponse.builder()
                .message("Rate limit exceeded")
                .status("RATE_LIMITED")
                .responseTime(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
}
