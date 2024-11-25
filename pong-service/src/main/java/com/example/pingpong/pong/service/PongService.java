package com.example.pingpong.pong.service;

import com.example.pingpong.pong.model.PongResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PongService {
    private final String instanceId;

    public PongService(@Value("${spring.application.name:pong-service}") String applicationName) {
        this.instanceId = applicationName + "-" + UUID.randomUUID().toString();
    }

    public String getInstanceId() {
        return instanceId;
    }

    public Mono<PongResponse> createPongResponse(String message) {
        return Mono.just(PongResponse.builder()
            .message("World!")
            .pongInstanceId(instanceId)
            .responseTime(LocalDateTime.now())
            .status("SUCCESS")
            .build());
    }
}
