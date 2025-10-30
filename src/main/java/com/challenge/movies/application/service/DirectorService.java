package com.challenge.movies.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.challenge.movies.domain.exception.MoviesFetchException;
import com.challenge.movies.domain.model.Movie;
import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.CachePort;
import com.challenge.movies.domain.port.MoviesPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

  private final MoviesPort moviesPort;
  private final AsyncMoviesService asyncMoviesService;
  private final CachePort cachePort;

  @Value("${cache.key-prefix}")
  private String cacheKeyPrefix;

  public List<String> getDirectorsByThreshold(Integer threshold) {
    List<Movie> allMovies = fetchAllMoviesInParallel();

    Map<String, Long> directorMovieCount =
        allMovies.stream().collect(Collectors.groupingBy(Movie::director, Collectors.counting()));

    return directorMovieCount.entrySet().stream()
        .filter(entry -> entry.getValue() > threshold)
        .map(Map.Entry::getKey)
        .sorted()
        .collect(Collectors.toList());
  }

  private List<Movie> fetchAllMoviesInParallel() {
    log.info("========== Starting Movies Fetch ==========");
    long startTime = System.currentTimeMillis();

    // First call to get total pages (with cache)
    long page1Start = System.currentTimeMillis();
    String page1Key = cacheKeyPrefix + 1;
    Optional<MoviesResponse> cachedPage1 = cachePort.get(page1Key, MoviesResponse.class);

    MoviesResponse firstPage;
    boolean page1FromCache = cachedPage1.isPresent();

    if (page1FromCache) {
      firstPage = cachedPage1.get();
      long page1Time = System.currentTimeMillis() - page1Start;
      log.info("Page 1: {} - {} ms", "Cache HIT", page1Time);
    } else {
      firstPage = moviesPort.getMoviesByPage(1);
      cachePort.put(page1Key, firstPage);
      long page1Time = System.currentTimeMillis() - page1Start;
      log.info("Page 1: {} - {} ms", "Fetched from API", page1Time);
    }

    List<Movie> allMovies = new ArrayList<>(firstPage.data());
    int totalPages = firstPage.totalPages();

    log.info("Total pages to fetch: {}", totalPages);

    if (totalPages > 1) {
      // Fetch remaining pages in parallel (cache checked inside AsyncMoviesService)
      long parallelStart = System.currentTimeMillis();

      List<CompletableFuture<MoviesResponse>> futures =
          IntStream.range(2, totalPages + 1)
              .mapToObj(asyncMoviesService::getMoviesByPageAsync)
              .toList();

      // Wait for all futures to complete
      CompletableFuture<Void> allFutures =
          CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

      try {
        allFutures.join();

        // Collect results from all futures
        futures.stream()
            .map(CompletableFuture::join)
            .forEach(response -> allMovies.addAll(response.data()));

        long parallelTime = System.currentTimeMillis() - parallelStart;
        log.info(
            "Parallel fetch of {} pages completed in {} ms (avg: {} ms/page)",
            (totalPages - 1),
            parallelTime,
            parallelTime / (totalPages - 1));

      } catch (Exception e) {
        log.error("Error fetching movies in parallel", e);
        throw new MoviesFetchException("Failed to fetch all movies", e);
      }
    }

    long endTime = System.currentTimeMillis();
    long totalTime = endTime - startTime;

    log.info("========== Fetch Complete ==========");
    log.info("Total movies fetched: {}", allMovies.size());
    log.info("Total pages: {}", totalPages);
    log.info(
        "Total time: {} ms (avg: {} ms/page)",
        totalTime,
        totalPages > 0 ? totalTime / totalPages : 0);
    log.info("=======================================");

    return allMovies;
  }
}
