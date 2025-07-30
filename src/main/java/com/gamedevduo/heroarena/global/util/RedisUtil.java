package com.gamedevduo.heroarena.global.util;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTemplate;
    public void setRefreshToken(String userId, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set("refresh:" + userId, refreshToken, ttlMillis, TimeUnit.MILLISECONDS);
    }

    public Optional<String> getRefreshToken(String userId) {
        Object token = redisTemplate.opsForValue().get("refresh:" + userId);
        return Optional.ofNullable(token != null ? token.toString() : null);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }



    public void setValue(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public Optional<String> getValue(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value != null ? value.toString() : null);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void addToList(String key, String value) {
        redisTemplate.opsForList().rightPush(key, value);
    }

    public void removeFromList(String key, String value) {
        redisTemplate.opsForList().remove(key, 1, value);
    }

    public List<String> getList(String key) {
        List<Object> list = redisTemplate.opsForList().range(key, 0, -1);
        if (list == null) return new ArrayList<>();
        return list.stream().map(Object::toString).collect(Collectors.toList());
    }
}
