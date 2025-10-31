package com.challenge.movies.infrastructure.adapter.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import com.challenge.movies.domain.model.MoviesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class RedisCacheAdapterTest {

  private static final String CACHE_KEY = "movies:page:1";
  private static final long TTL_MINUTES = 30L;

  @Mock private RedisTemplate<String, Object> redisTemplate;

  @Mock private ObjectMapper objectMapper;

  @Mock private ValueOperations<String, Object> valueOperations;

  @InjectMocks private RedisCacheAdapter redisCacheAdapter;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(redisCacheAdapter, "ttlMinutes", TTL_MINUTES);
  }

  @Test
  void returnValueFromRedisWhenCacheHit() {
    // Given
    Object cachedValue = new Object();
    MoviesResponse expectedResponse = mock(MoviesResponse.class);

    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenReturn(cachedValue);
    when(objectMapper.convertValue(cachedValue, MoviesResponse.class)).thenReturn(expectedResponse);

    // When
    Optional<MoviesResponse> result = redisCacheAdapter.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(expectedResponse);
    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).get(CACHE_KEY);
    verify(objectMapper, times(1)).convertValue(cachedValue, MoviesResponse.class);
  }

  @Test
  void returnEmptyOptionalWhenCacheMiss() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenReturn(null);

    // When
    Optional<MoviesResponse> result = redisCacheAdapter.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isEmpty();
    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).get(CACHE_KEY);
    verify(objectMapper, times(0)).convertValue(any(), eq(MoviesResponse.class));
  }

  @Test
  void returnEmptyOptionalWhenRedisThrowsException() {
    // Given
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    when(valueOperations.get(CACHE_KEY)).thenThrow(new RuntimeException("Redis connection error"));

    // When
    Optional<MoviesResponse> result = redisCacheAdapter.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isEmpty();
    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).get(CACHE_KEY);
  }

  @Test
  void putValueInRedisSuccessfully() {
    // Given
    MoviesResponse value = mock(MoviesResponse.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);

    // When
    redisCacheAdapter.put(CACHE_KEY, value);

    // Then
    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).set(CACHE_KEY, value, Duration.ofMinutes(TTL_MINUTES));
  }

  @Test
  void doNothingWhenPutThrowsException() {
    // Given
    MoviesResponse value = mock(MoviesResponse.class);
    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    doThrow(new RuntimeException("Redis error"))
        .when(valueOperations)
        .set(anyString(), any(), any(Duration.class));

    // When - exception is caught and logged
    redisCacheAdapter.put(CACHE_KEY, value);

    // Then
    verify(redisTemplate, times(1)).opsForValue();
    verify(valueOperations, times(1)).set(CACHE_KEY, value, Duration.ofMinutes(TTL_MINUTES));
  }

  @Test
  void evictValueFromRedisSuccessfully() {
    // When
    redisCacheAdapter.evict(CACHE_KEY);

    // Then
    verify(redisTemplate, times(1)).delete(CACHE_KEY);
  }

  @Test
  void doNothingWhenEvictThrowsException() {
    // Given
    when(redisTemplate.delete(CACHE_KEY)).thenThrow(new RuntimeException("Redis delete error"));

    // When - exception is caught and logged
    redisCacheAdapter.evict(CACHE_KEY);

    // Then
    verify(redisTemplate, times(1)).delete(CACHE_KEY);
  }

  @Test
  void clearAllRedisCacheSuccessfully() {
    // Given
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
    RedisConnection connection = mock(RedisConnection.class);
    RedisServerCommands serverCommands = mock(RedisServerCommands.class);

    when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
    when(connectionFactory.getConnection()).thenReturn(connection);
    when(connection.serverCommands()).thenReturn(serverCommands);

    // When
    redisCacheAdapter.clear();

    // Then
    verify(redisTemplate, times(1)).getConnectionFactory();
    verify(connectionFactory, times(1)).getConnection();
    verify(connection, times(1)).serverCommands();
    verify(serverCommands, times(1)).flushAll();
  }

  @Test
  void doNothingWhenConnectionFactoryIsNull() {
    // Given
    when(redisTemplate.getConnectionFactory()).thenReturn(null);

    // When
    redisCacheAdapter.clear();

    // Then
    verify(redisTemplate, times(1)).getConnectionFactory();
  }

  @Test
  void doNothingWhenClearThrowsException() {
    // Given
    RedisConnectionFactory connectionFactory = mock(RedisConnectionFactory.class);
    when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
    when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection error"));

    // When - exception is caught and logged
    redisCacheAdapter.clear();

    // Then
    verify(redisTemplate, times(1)).getConnectionFactory();
    verify(connectionFactory, times(1)).getConnection();
  }
}
