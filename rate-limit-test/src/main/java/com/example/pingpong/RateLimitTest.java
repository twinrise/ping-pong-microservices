package com.example.pingpong;

import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitTest {
    private static final String BASE_URL = "http://localhost:8081";
    private static final WebClient webClient = WebClient.create(BASE_URL);
    private static final AtomicInteger successCount = new AtomicInteger(0);
    private static final AtomicInteger failureCount = new AtomicInteger(0);

    public static void main(String[] args) {
        System.out.println("Starting rate limit test...");
        
        // 创建100个并发请求
        Flux.range(1, 100)
            .flatMap(i -> makeRequest(i)
                .doOnSuccess(response -> {
                    successCount.incrementAndGet();
                    System.out.println("Request " + i + " succeeded with response: " + response);
                })
                .doOnError(error -> {
                    failureCount.incrementAndGet();
                    System.out.println("Request " + i + " failed with error: " + error.getMessage());
                })
                .onErrorResume(e -> Mono.empty()), 
                10) // 最大并发数为10
            .blockLast(Duration.ofSeconds(30));

        System.out.println("\nTest completed!");
        System.out.println("Successful requests: " + successCount.get());
        System.out.println("Failed requests (rate limited): " + failureCount.get());
    }

    private static Mono<String> makeRequest(int requestNumber) {
        return webClient
            .get()
            .uri("/pong")
            .retrieve()
            .bodyToMono(String.class)
            .doOnSubscribe(s -> System.out.println("Sending request " + requestNumber));
    }
}
