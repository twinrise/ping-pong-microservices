package com.example.pingpong.ping.config;

import com.example.pingpong.ping.service.PingService;
import com.example.pingpong.ping.ratelimit.HazelcastRateLimiter;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PingConfiguration {
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public PingService pingService(
            WebClient.Builder webClientBuilder,
            RocketMQTemplate rocketMQTemplate,
            HazelcastRateLimiter rateLimiter) {
        return new PingService(webClientBuilder, rocketMQTemplate, rateLimiter);
    }
}
