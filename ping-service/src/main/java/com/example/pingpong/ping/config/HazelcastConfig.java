package com.example.pingpong.ping.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HazelcastConfig {

    @Value("${rate-limit.max-requests}")
    private int maxRequests;

    @Value("${rate-limit.time-window}")
    private int timeWindow;

    @Bean("pingServiceHazelcastConfig")
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("ping-service-hazelcast");
        
        // Network configuration
        NetworkConfig networkConfig = config.getNetworkConfig();
        networkConfig.setPort(5701);
        networkConfig.setPortAutoIncrement(true);
        
        // Join configuration
        JoinConfig joinConfig = networkConfig.getJoin();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig()
                .setEnabled(true)
                .addMember("localhost");

        // Map configuration for rate limiting
        MapConfig mapConfig = new MapConfig("rate-limit-map")
                .setTimeToLiveSeconds(timeWindow)
                .setMaxIdleSeconds(timeWindow);
        
        config.addMapConfig(mapConfig);
        
        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(@Qualifier("pingServiceHazelcastConfig") Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }
}
