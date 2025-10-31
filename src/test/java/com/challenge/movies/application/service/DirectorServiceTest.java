package com.challenge.movies.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.challenge.movies.domain.exception.MoviesFetchException;
import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.CachePort;
import com.challenge.movies.domain.port.MoviesPort;
import com.challenge.movies.utils.DummyData;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

  private static final int THRESHOLD_4 = 4;
  private static final int THRESHOLD_0 = 0;
  private static final int PAGE_1 = 1;
  private static final int PAGE_2 = 2;
  private static final int PAGE_3 = 3;
  private static final String CACHE_KEY_PREFIX = "movies:page:";

  @Mock private MoviesPort moviesPort;

  @Mock private AsyncMoviesService asyncMoviesService;

  @Mock private CachePort cachePort;

  @InjectMocks private DirectorService directorService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(directorService, "cacheKeyPrefix", CACHE_KEY_PREFIX);
  }

  @Test
  void returnDirectorsSortedAlphabeticallyWhenThresholdIs4() {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_1)).thenReturn(page1);
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_2))
        .thenReturn(CompletableFuture.completedFuture(page2));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_3))
        .thenReturn(CompletableFuture.completedFuture(page3));

    // When
    List<String> result = directorService.getDirectorsByThreshold(THRESHOLD_4);

    // Then
    assertThat(result).containsExactly("Martin Scorsese", "Woody Allen");
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_1);
    verify(cachePort, times(1)).put(CACHE_KEY_PREFIX + PAGE_1, page1);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_2);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_3);
  }

  @Test
  void returnAllDirectorsWhenThresholdIsZero() {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_1)).thenReturn(page1);
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_2))
        .thenReturn(CompletableFuture.completedFuture(page2));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_3))
        .thenReturn(CompletableFuture.completedFuture(page3));

    // When
    List<String> result = directorService.getDirectorsByThreshold(THRESHOLD_0);

    // Then
    assertThat(result)
        .containsExactly(
            "Clint Eastwood",
            "Juan José Campanella",
            "M. Night Shyamalan",
            "Martin Scorsese",
            "Pedro Almodóvar",
            "Quentin Tarantino",
            "Woody Allen");
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_1);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_2);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_3);
  }

  @Test
  void returnDirectorsFromCacheWhenCacheHit() {
    // Given
    MoviesResponse cachedPage1 =
        DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class))
        .thenReturn(Optional.of(cachedPage1));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_2))
        .thenReturn(CompletableFuture.completedFuture(page2));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_3))
        .thenReturn(CompletableFuture.completedFuture(page3));

    // When
    List<String> result = directorService.getDirectorsByThreshold(THRESHOLD_4);

    // Then
    assertThat(result).containsExactly("Martin Scorsese", "Woody Allen");
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class);
    verify(moviesPort, times(0)).getMoviesByPage(anyInt());
    verify(cachePort, times(0)).put(anyString(), any());
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_2);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_3);
  }

  @Test
  void returnEmptyListWhenNoDirectorsExceedThreshold() {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_1)).thenReturn(page1);
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_2))
        .thenReturn(CompletableFuture.completedFuture(page2));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_3))
        .thenReturn(CompletableFuture.completedFuture(page3));

    // When
    List<String> result = directorService.getDirectorsByThreshold(10);

    // Then
    assertThat(result).isEmpty();
    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_1);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_2);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_3);
  }

  @Test
  void throwMoviesFetchExceptionWhenAsyncFetchFails() {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    when(cachePort.get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class))
        .thenReturn(Optional.empty());
    when(moviesPort.getMoviesByPage(PAGE_1)).thenReturn(page1);
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_2))
        .thenReturn(CompletableFuture.failedFuture(new RuntimeException("API error")));
    when(asyncMoviesService.getMoviesByPageAsync(PAGE_3))
        .thenReturn(CompletableFuture.completedFuture(page3));

    // When / Then
    assertThatThrownBy(() -> directorService.getDirectorsByThreshold(THRESHOLD_4))
        .isInstanceOf(MoviesFetchException.class)
        .hasMessage("Failed to fetch all movies")
        .hasCauseInstanceOf(RuntimeException.class);

    verify(cachePort, times(1)).get(CACHE_KEY_PREFIX + PAGE_1, MoviesResponse.class);
    verify(moviesPort, times(1)).getMoviesByPage(PAGE_1);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_2);
    verify(asyncMoviesService, times(1)).getMoviesByPageAsync(PAGE_3);
  }
}
