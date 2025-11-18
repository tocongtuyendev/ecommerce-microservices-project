package com.yourcompany.ecommerce.search.service;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


import java.time.Duration;


@Service
public class CacheService {


    private final ReactiveRedisTemplate<String, String> redis;


    public CacheService(ReactiveRedisTemplate<String, String> redis) {
        this.redis = redis;
    }


    public Mono<String> get(String key) {
        return redis.opsForValue().get(key);
    }


    public Mono<Boolean> put(String key, String value, Duration ttl) {
        return redis.opsForValue().set(key, value).flatMap(ok -> redis.expire(key, ttl));
    }


    public Mono<Boolean> delete(String key) {
        return redis.opsForValue().delete(key).map(count -> count);
    }
}