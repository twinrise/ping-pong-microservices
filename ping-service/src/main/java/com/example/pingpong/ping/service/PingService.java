package com.example.pingpong.ping.service;

import com.example.pingpong.shared.ServiceIdentifier;
import com.example.pingpong.shared.message.PingMessage;
import com.example.pingpong.shared.message.PingLogMessage;
import com.example.pingpong.ping.ratelimit.HazelcastRateLimiter;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

public class PingService implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(PingService.class);
    private final WebClient webClient;
    private final RocketMQTemplate rocketMQTemplate;
    private final ServiceIdentifier serviceId;
    private final HazelcastRateLimiter rateLimiter;
    private final String processId;
    
    @Value("${app.service.name:ping-service}")
    private String serviceName;
    
    @Value("${pong-service.url}")
    private String pongServiceBaseUrl;
    
    @Value("${pong-service.endpoints.pong}")
    private String pongEndpoint;
    
    @Value("${pong-service.retry.max-attempts}")
    private int maxAttempts;
    
    @Value("${pong-service.retry.initial-backoff}")
    private long initialBackoff;

    public PingService(WebClient.Builder webClientBuilder, 
                      RocketMQTemplate rocketMQTemplate,
                      HazelcastRateLimiter rateLimiter) {
        this.webClient = webClientBuilder.build();
        this.rocketMQTemplate = rocketMQTemplate;
        this.rateLimiter = rateLimiter;
        this.processId = String.valueOf(ProcessHandle.current().pid());
        this.serviceId = new ServiceIdentifier("ping-service");
        logger.info("PingService instance started with ID: {}, Process ID: {}", serviceId, processId);
    }

    @Override
    public void run(String... args) {
        logger.info("Service name set to: {}", serviceName);
        logger.info("Starting ping service loop...");
        Mono.defer(this::sendPing)
            .repeat()
            .delayElements(Duration.ofSeconds(1))
            .retryWhen(Retry.backoff(Long.MAX_VALUE, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(5)))
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(
                response -> logger.info("Received response: {}", response),
                error -> logger.error("Error in ping loop: ", error)
            );
    }

    private Mono<String> sendPing() {
        if (!rateLimiter.tryAcquire()) {
            PingLogMessage logMessage = PingLogMessage.error(
                serviceName,
                serviceId.getInstanceId(),
                "Client Rate limit exceeded"
            );
            logMessage.setStatus("未发送");
            logMessage.setProcessId(processId);
            rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", logMessage);
            logger.warn("[{}] Request rate limited", serviceId);
            return Mono.error(new RuntimeException("Rate limit exceeded"));
        }

        return webClient.get()
            .uri(uriBuilder -> uriBuilder
                .scheme("http")
                .host("localhost")
                .port(8081)
                .path(pongEndpoint + "/Hello")
                .build())
            .retrieve()
            .bodyToMono(String.class)
            .doOnSuccess(response -> {
                PingLogMessage logMessage = PingLogMessage.success(
                    serviceName,
                    serviceId.getInstanceId(),
                    response
                );
                logMessage.setProcessId(processId);
                rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", logMessage);
                logger.info("[{}] Successfully sent ping and received response: {}", 
                    serviceId, response);
            })
            .onErrorResume(error -> {
                if (error instanceof WebClientResponseException) {
                    WebClientResponseException wcError = (WebClientResponseException) error;
                    String responseBody = wcError.getResponseBodyAsString();
                    PingLogMessage logMessage = PingLogMessage.error(
                        serviceName,
                        serviceId.getInstanceId(),
                        wcError.getMessage()
                    );
                    logMessage.setResponse(responseBody);
                    logMessage.setStatus("已发送");
                    logMessage.setProcessId(processId);
                    rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", logMessage);
                    logger.error("[{}] Failed to send ping with response: {}", 
                        serviceId, responseBody);
                } else {
                    PingLogMessage logMessage = PingLogMessage.error(
                        serviceName,
                        serviceId.getInstanceId(),
                        error.getMessage()
                    );
                    logMessage.setProcessId(processId);
                    rocketMQTemplate.sendOneWay("PING_LOG_TOPIC", logMessage);
                    logger.error("[{}] Failed to send ping: {}", 
                        serviceId, error.getMessage());
                }
                return Mono.error(error);
            })
            .timeout(Duration.ofSeconds(5));
    }
}
