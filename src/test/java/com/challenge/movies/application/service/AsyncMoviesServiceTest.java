package com.challenge.movies.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.CachePort;
import com.challenge.movies.domain.port.MoviesPort;
import com.challenge.movies.utils.DummyData;

@ExtendWith(MockitoExtension.class)
class AsyncMoviesServiceTest {

  private static final int PAGE_2 = 2;
  private static final String CACHE_KEY_PREFIX = "movies:page:";

  @Mock private MoviesPort moviesPort;

  @Mock private CachePort cachePort;

  @InjectMocks private AsyncMoviesService asyncMoviesService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(asyncMoviesService, "cacheKeyPrefix", CACHE_KEY_PREFIX);
  }

  @Test
  void returnMoviesResponseFromCacheWhenCacheHit() throws ExecutionException, InterruptedException {
    // Given
    MoviesResponse cachedResponse =
        DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class))
        .thenReturn(Optional.of(cachedResponse));

    // When
    CompletableFuture<MoviesResponse> result = asyncMoviesService.getMoviesByPageAsync(PAGE_2);

    // Then
    assertThat(result.get()).isEqualTo(cachedResponse);
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class);
    verify(moviesPort, times(0)).getMoviesByPage(PAGE_2);
    verify(cachePort, times(0)).put(eq(CACHE_KEY_PREFIX + PAGE_2), eq(cachedResponse));
  }

  @Test
  void returnMoviesResponseFromApiWhenCacheMiss() throws ExecutionException, InterruptedException {
    // Given
    MoviesResponse apiResponse =
        DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_2)).thenReturn(apiResponse);

    // When
    CompletableFuture<MoviesResponse> result = asyncMoviesService.getMoviesByPageAsync(PAGE_2);

    // Then
    assertThat(result.get()).isEqualTo(apiResponse);
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_2);
    verify(cachePort, times(1)).put(CACHE_KEY_PREFIX + PAGE_2, apiResponse);
  }

  @Test
  void returnFailedFutureWhenMoviesPortThrowsException() {
    // Given
    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_2))
        .thenThrow(new RuntimeException("External API failure"));

    // When
    CompletableFuture<MoviesResponse> result = asyncMoviesService.getMoviesByPageAsync(PAGE_2);

    // Then
    assertThat(result.isCompletedExceptionally()).isTrue();
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_2);
    verify(cachePort, times(0)).put(eq(CACHE_KEY_PREFIX + PAGE_2), eq(null));
  }

  @Test
  void returnFailedFutureWhenCachePortThrowsException() {
    // Given
    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class))
        .thenThrow(new RuntimeException("Cache connection error"));

    // When
    CompletableFuture<MoviesResponse> result = asyncMoviesService.getMoviesByPageAsync(PAGE_2);

    // Then
    assertThat(result.isCompletedExceptionally()).isTrue();
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_2, MoviesResponse.class);
    verify(moviesPort, times(0)).getMoviesByPage(PAGE_2);
  }
}
