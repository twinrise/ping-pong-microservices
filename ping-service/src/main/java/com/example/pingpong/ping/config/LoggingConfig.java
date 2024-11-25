package com.example.pingpong.ping.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class LoggingConfig {
    private static final Logger logger = LoggerFactory.getLogger(LoggingConfig.class);

    @Value("${app.service.name}")
    private String serviceName;

    @PostConstruct
    public void init() {
        // 创建日志目录
        File logDir = new File("logs");
        if (!logDir.exists()) {
            if (logDir.mkdirs()) {
                logger.info("Created logs directory");
            } else {
                logger.error("Failed to create logs directory");
            }
        }
        logger.info("Service name: {}", serviceName);
        logger.info("Log file: logs/{}.log", serviceName);
    }
}
