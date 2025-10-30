package com.challenge.movies.infrastructure.adapter.cache;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.challenge.movies.domain.port.CachePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("local")
public class CaffeineCacheAdapter implements CachePort {

  private final Cache<String, Object> cache;
  private final ObjectMapper objectMapper;

  public CaffeineCacheAdapter(
      @Value("${cache.ttl-minutes:30}") long ttlMinutes,
      @Value("${cache.max-size:1000}") long maxSize,
      ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.cache =
        Caffeine.newBuilder()
            .expireAfterWrite(ttlMinutes, TimeUnit.MINUTES)
            .maximumSize(maxSize)
            .recordStats()
            .build();

    log.info("Initialized Caffeine cache - TTL: {} minutes, Max size: {}", ttlMinutes, maxSize);
  }

  @Override
  public <T> Optional<T> get(String key, Class<T> type) {
    try {
      Object value = cache.getIfPresent(key);
      if (value != null) {
        log.debug("Cache HIT for key: {}", key);
        return Optional.of(objectMapper.convertValue(value, type));
      }
      log.debug("Cache MISS for key: {}", key);
      return Optional.empty();
    } catch (Exception e) {
      log.error("Error getting value from cache for key: {}", key, e);
      return Optional.empty();
    }
  }

  @Override
  public <T> void put(String key, T value) {
    try {
      cache.put(key, value);
      log.debug("Cached value for key: {}", key);
    } catch (Exception e) {
      log.error("Error putting value in cache for key: {}", key, e);
    }
  }

  @Override
  public void evict(String key) {
    cache.invalidate(key);
    log.debug("Evicted cache for key: {}", key);
  }

  @Override
  public void clear() {
    cache.invalidateAll();
    log.info("Cleared all cache");
  }
}
