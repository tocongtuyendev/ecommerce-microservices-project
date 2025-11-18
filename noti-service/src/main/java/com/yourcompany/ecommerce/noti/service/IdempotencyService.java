package com.yourcompany.ecommerce.noti.service;

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


    public boolean isProcessed(String id) {
        String key = prefix + ":" + id;
        Boolean exists = redis.hasKey(key);
        return exists != null && exists;
    }


    public void markProcessed(String id, Duration ttl) {
        String key = prefix + ":" + id;
        redis.opsForValue().set(key, "1", ttl);
    }
}