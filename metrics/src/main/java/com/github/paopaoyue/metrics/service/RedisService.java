package com.github.paopaoyue.metrics.service;

import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;

@Service
@ConditionalOnProperty(value = "spring.data.redis.host")
public class RedisService {

    private static final Logger logger = LogManager.getLogger(RedisService.class);

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        if (!isAlive()) {
            logger.error("Redis service is not available");
        }
    }

    public boolean isAlive() {
        try {
            String pingResponse = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().ping();
            return "PONG".equalsIgnoreCase(pingResponse);
        } catch (Exception e) {
            return false;
        }
    }

    public <T> void set(String key, T value, Duration duration) {
        redisTemplate.opsForValue().set(key, value, duration);
    }

    public <T> T get(String key) {
        return (T) redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }
}
