package com.example.pingpong.ping.ratelimit;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.io.Serializable;

@Component
public class HazelcastRateLimiter {
    private static final Logger logger = LoggerFactory.getLogger(HazelcastRateLimiter.class);
    private static final String MAP_NAME = "rate-limit-map";
    private static final String COUNTER_KEY = "request-counter";
    
    private final HazelcastInstance hazelcastInstance;
    private final int maxRequestsPerSecond;
    private final Duration windowDuration;

    public HazelcastRateLimiter(
            HazelcastInstance hazelcastInstance,
            @Value("${rate-limit.max-requests}") int maxRequestsPerSecond,
            @Value("${rate-limit.time-window}") int timeWindowSeconds) {
        this.hazelcastInstance = hazelcastInstance;
        this.maxRequestsPerSecond = maxRequestsPerSecond;
        this.windowDuration = Duration.ofSeconds(timeWindowSeconds);
        logger.info("Rate limiter initialized with max requests per second: {}, window duration: {}", 
            maxRequestsPerSecond, windowDuration);
    }

    public boolean tryAcquire() {
        IMap<String, HazelcastRateLimiter.RateLimitEntry> rateLimitMap = hazelcastInstance.getMap(MAP_NAME);
        
        try {
            rateLimitMap.lock(COUNTER_KEY);
            return tryAcquireUnderLock(rateLimitMap);
        } finally {
            rateLimitMap.unlock(COUNTER_KEY);
        }
    }

    private boolean tryAcquireUnderLock(IMap<String, HazelcastRateLimiter.RateLimitEntry> rateLimitMap) {
        HazelcastRateLimiter.RateLimitEntry entry = rateLimitMap.get(COUNTER_KEY);
        Instant now = Instant.now();
        long currentSecond = now.getEpochSecond();

        if (entry == null || entry.getSecond() != currentSecond) {
            entry = new HazelcastRateLimiter.RateLimitEntry(currentSecond, 1);
            rateLimitMap.put(COUNTER_KEY, entry);
            logger.debug("New rate limit entry created for second: {}", currentSecond);
            return true;
        }

        if (entry.getCount() >= maxRequestsPerSecond) {
            logger.debug("Rate limit exceeded for second: {}. Current count: {}", 
                currentSecond, entry.getCount());
            return false;
        }

        entry = new HazelcastRateLimiter.RateLimitEntry(currentSecond, entry.getCount() + 1);
        rateLimitMap.put(COUNTER_KEY, entry);
        logger.debug("Request accepted for second: {}. Current count: {}", 
            currentSecond, entry.getCount());
        return true;
    }

    public static class RateLimitEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        private final long second;
        private final int count;

        public RateLimitEntry(long second, int count) {
            this.second = second;
            this.count = count;
        }

        public long getSecond() {
            return second;
        }

        public int getCount() {
            return count;
        }
    }
}
