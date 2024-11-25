package com.example.pingpong.ping.config;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(RocketMQAutoConfiguration.class)
public class RocketMQConfig {
    // The RocketMQTemplate bean will be automatically created by RocketMQAutoConfiguration
    // as long as the rocketmq.name-server property is set in application.properties
}
