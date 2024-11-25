package com.example.pingpong.logservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.CommandLineRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EntityScan("com.example.pingpong.shared.entity")
@EnableJpaRepositories("com.example.pingpong.shared.repository")
public class LogServiceApplication {
    private static final Logger logger = LoggerFactory.getLogger(LogServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(LogServiceApplication.class, args);
        logger.info("Log Service is starting...");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

    @Bean
    public CommandLineRunner runner() {
        return args -> {
            logger.info("Log Service started successfully");
            // 使用一个无限循环来保持应用运行
            Thread.sleep(Long.MAX_VALUE);
        };
    }
}
