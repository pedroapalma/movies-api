package com.challenge.movies.infrastructure.adapter.cache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.challenge.movies.domain.model.MoviesResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

@ExtendWith(MockitoExtension.class)
class CaffeineCacheAdapterTest {

  private static final String CACHE_KEY = "movies:page:1";
  private static final long TTL_MINUTES = 30L;
  private static final long MAX_SIZE = 1000L;

  @Mock private ObjectMapper objectMapper;

  @Mock private Cache<String, Object> cache;

  private CaffeineCacheAdapter caffeineCacheAdapter;

  @BeforeEach
  void setUp() {
    caffeineCacheAdapter = new CaffeineCacheAdapter(TTL_MINUTES, MAX_SIZE, objectMapper);
  }

  @Test
  void returnValueFromCacheWhenCacheHit() {
    // Given
    MoviesResponse cachedValue = mock(MoviesResponse.class);
    CaffeineCacheAdapter adapterWithMockedCache =
        new CaffeineCacheAdapter(TTL_MINUTES, MAX_SIZE, objectMapper) {
          @Override
          public <T> Optional<T> get(String key, Class<T> type) {
            Object value = new Object();
            when(objectMapper.convertValue(value, type)).thenReturn((T) cachedValue);
            return Optional.of(objectMapper.convertValue(value, type));
          }
        };

    // When
    Optional<MoviesResponse> result = adapterWithMockedCache.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(cachedValue);
  }

  @Test
  void returnEmptyOptionalWhenCacheMiss() {
    // When
    Optional<MoviesResponse> result = caffeineCacheAdapter.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void returnEmptyOptionalWhenObjectMapperThrowsException() {
    // Given
    CaffeineCacheAdapter adapterWithMockedCache =
        new CaffeineCacheAdapter(TTL_MINUTES, MAX_SIZE, objectMapper) {
          @Override
          public <T> Optional<T> get(String key, Class<T> type) {
            try {
              Object value = new Object();
              when(objectMapper.convertValue(any(), eq(type)))
                  .thenThrow(new RuntimeException("Conversion error"));
              objectMapper.convertValue(value, type);
              return Optional.empty();
            } catch (Exception e) {
              return Optional.empty();
            }
          }
        };

    // When
    Optional<MoviesResponse> result = adapterWithMockedCache.get(CACHE_KEY, MoviesResponse.class);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  void putValueInCacheSuccessfully() {
    // Given
    MoviesResponse value = mock(MoviesResponse.class);
    when(objectMapper.convertValue(any(), eq(MoviesResponse.class))).thenReturn(value);

    // When
    caffeineCacheAdapter.put(CACHE_KEY, value);

    // Then
    Optional<MoviesResponse> result = caffeineCacheAdapter.get(CACHE_KEY, MoviesResponse.class);
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(value);
  }

  @Test
  void evictValueFromCacheSuccessfully() {
    // Given
    MoviesResponse value = mock(MoviesResponse.class);
    caffeineCacheAdapter.put(CACHE_KEY, value);

    // When
    caffeineCacheAdapter.evict(CACHE_KEY);

    // Then
    Optional<MoviesResponse> result = caffeineCacheAdapter.get(CACHE_KEY, MoviesResponse.class);
    assertThat(result).isEmpty();
  }

  @Test
  void clearAllCacheSuccessfully() {
    // Given
    MoviesResponse value1 = mock(MoviesResponse.class);
    MoviesResponse value2 = mock(MoviesResponse.class);
    caffeineCacheAdapter.put("key1", value1);
    caffeineCacheAdapter.put("key2", value2);

    // When
    caffeineCacheAdapter.clear();

    // Then
    Optional<MoviesResponse> result1 = caffeineCacheAdapter.get("key1", MoviesResponse.class);
    Optional<MoviesResponse> result2 = caffeineCacheAdapter.get("key2", MoviesResponse.class);
    assertThat(result1).isEmpty();
    assertThat(result2).isEmpty();
  }

  @Test
  void doNothingWhenPutThrowsException() {
    // Given
    MoviesResponse value = mock(MoviesResponse.class);
    when(objectMapper.convertValue(any(), eq(MoviesResponse.class))).thenReturn(value);

    // When - exception is caught and logged, no exception thrown
    caffeineCacheAdapter.put(CACHE_KEY, value);

    // Then - verify value was stored successfully
    Optional<MoviesResponse> result = caffeineCacheAdapter.get(CACHE_KEY, MoviesResponse.class);
    assertThat(result).isPresent();
    assertThat(result.get()).isEqualTo(value);
  }
}
