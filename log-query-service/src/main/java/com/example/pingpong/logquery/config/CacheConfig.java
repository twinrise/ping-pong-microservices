package com.example.pingpong.logquery.config;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.support.SimpleCacheManager;

@Configuration
@EnableCaching
public class CacheConfig {
    @Bean
    public Config hazelcastConfig() {
        Config config = new Config();
        config.setInstanceName("hazelcast-instance");

        NetworkConfig network = config.getNetworkConfig();
        network.setPort(5701);
        network.setPortAutoIncrement(true);

        JoinConfig join = network.getJoin();
        join.getMulticastConfig().setEnabled(false);
        join.getTcpIpConfig()
            .setEnabled(true)
            .addMember("localhost");

        MapConfig logCache = new MapConfig();
        logCache.setName("logs")
               .setEvictionConfig(new EvictionConfig()
                   .setEvictionPolicy(EvictionPolicy.LRU)
                   .setSize(1000)
                   .setMaxSizePolicy(MaxSizePolicy.PER_NODE))
               .setTimeToLiveSeconds(300);

        config.addMapConfig(logCache);

        return config;
    }

    @Bean
    public HazelcastInstance hazelcastInstance(Config hazelcastConfig) {
        return Hazelcast.newHazelcastInstance(hazelcastConfig);
    }
}
