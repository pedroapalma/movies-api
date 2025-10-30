package com.challenge.movies.application.service;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.CachePort;
import com.challenge.movies.domain.port.MoviesPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMoviesService {

  private final MoviesPort moviesPort;
  private final CachePort cachePort;

  @Value("${cache.key-prefix}")
  private String cacheKeyPrefix;

  @Async("moviesTaskExecutor")
  public CompletableFuture<MoviesResponse> getMoviesByPageAsync(Integer page) {
    long startTime = System.currentTimeMillis();
    try {
      String cacheKey = cacheKeyPrefix + page;

      // Try to get from cache
      Optional<MoviesResponse> cachedResponse = cachePort.get(cacheKey, MoviesResponse.class);

      if (cachedResponse.isPresent()) {
        long fetchTime = System.currentTimeMillis() - startTime;
        log.info("Page {}: Cache HIT - {} ms", page, fetchTime);
        return CompletableFuture.completedFuture(cachedResponse.get());
      }

      // Fetch from API if not in cache
      MoviesResponse response = moviesPort.getMoviesByPage(page);

      // Store in cache
      cachePort.put(cacheKey, response);

      long fetchTime = System.currentTimeMillis() - startTime;
      log.info("Page {}: Fetched from API - {} ms", page, fetchTime);

      return CompletableFuture.completedFuture(response);
    } catch (Exception e) {
      log.error("Error fetching page {}: {}", page, e.getMessage());
      return CompletableFuture.failedFuture(e);
    }
  }
}
