package com.example.pingpong.logquery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = {"com.example.pingpong.shared.entity"})
@EnableJpaRepositories(basePackages = {"com.example.pingpong.logquery.repository"})
public class LogQueryApplication {
    public static void main(String[] args) {
        SpringApplication.run(LogQueryApplication.class, args);
    }
}
