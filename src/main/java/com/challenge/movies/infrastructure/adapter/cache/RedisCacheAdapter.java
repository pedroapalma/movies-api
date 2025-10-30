package com.challenge.movies.infrastructure.adapter.cache;

import java.time.Duration;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.challenge.movies.domain.port.CachePort;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("!local")
@RequiredArgsConstructor
public class RedisCacheAdapter implements CachePort {

  private final RedisTemplate<String, Object> redisTemplate;
  private final ObjectMapper objectMapper;

  @Value("${cache.ttl-minutes:30}")
  private long ttlMinutes;

  @Override
  public <T> Optional<T> get(String key, Class<T> type) {
    try {
      Object value = redisTemplate.opsForValue().get(key);
      if (value == null) {
        log.debug("Redis cache MISS for key: {}", key);
        return Optional.empty();
      }
      log.debug("Redis cache HIT for key: {}", key);
      return Optional.of(objectMapper.convertValue(value, type));
    } catch (Exception e) {
      log.error("Error getting value from Redis cache for key: {}", key, e);
      return Optional.empty();
    }
  }

  @Override
  public <T> void put(String key, T value) {
    try {
      redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(ttlMinutes));
      log.debug("Cached value in Redis for key: {} with TTL: {} minutes", key, ttlMinutes);
    } catch (Exception e) {
      log.error("Error putting value in Redis cache for key: {}", key, e);
    }
  }

  @Override
  public void evict(String key) {
    try {
      redisTemplate.delete(key);
      log.debug("Evicted Redis cache for key: {}", key);
    } catch (Exception e) {
      log.error("Error evicting Redis cache for key: {}", key, e);
    }
  }

  @Override
  public void clear() {
    try {
      var connectionFactory = redisTemplate.getConnectionFactory();
      if (connectionFactory == null) {
        log.warn("Redis connection factory is null, cannot clear cache");
        return;
      }

      connectionFactory.getConnection().serverCommands().flushAll();
      log.info("Cleared all Redis cache");
    } catch (Exception e) {
      log.error("Error clearing Redis cache", e);
    }
  }
}
