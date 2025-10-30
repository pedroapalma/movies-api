package com.challenge.movies.application.service;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.MoviesPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncMoviesService {

  private final MoviesPort moviesPort;

  @Async("moviesTaskExecutor")
  public CompletableFuture<MoviesResponse> getMoviesByPageAsync(Integer page) {
    log.debug(
        "Fetching movies page {} asynchronously on thread: {}",
        page,
        Thread.currentThread().getName());
    try {
      MoviesResponse response = moviesPort.getMoviesByPage(page);
      log.debug("Successfully fetched page {}", page);
      return CompletableFuture.completedFuture(response);
    } catch (Exception e) {
      log.error("Error fetching page {}: {}", page, e.getMessage());
      return CompletableFuture.failedFuture(e);
    }
  }
}
