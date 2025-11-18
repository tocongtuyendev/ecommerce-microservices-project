package com.yourcompany.ecommerce.sync.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.Duration;


@Service
public class IdempotencyService {


    private final RedisTemplate<String, Object> redis;
    private final String prefix;


    public IdempotencyService(RedisTemplate<String, Object> redis, @Value("${app.idempotency.prefix}") String prefix) {
        this.redis = redis;
        this.prefix = prefix;
    }


    public boolean isProcessed(String eventId) {
        String key = prefix + ":" + eventId;
        Boolean exists = redis.hasKey(key);
        return exists != null && exists;
    }


    public void markProcessed(String eventId, Duration ttl) {
        String key = prefix + ":" + eventId;
        redis.opsForValue().set(key, "1", ttl);
    }
}