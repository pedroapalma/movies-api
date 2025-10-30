package com.challenge.movies.application.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

import com.challenge.movies.domain.model.Movie;
import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.MoviesPort;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectorService {

  private final MoviesPort moviesPort;
  private final AsyncMoviesService asyncMoviesService;

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
    log.info("Starting parallel fetch of all movies");
    long startTime = System.currentTimeMillis();

    // First call to get total pages
    MoviesResponse firstPage = moviesPort.getMoviesByPage(1);
    List<Movie> allMovies = new ArrayList<>(firstPage.data());
    int totalPages = firstPage.totalPages();

    log.info("Total pages to fetch: {}", totalPages);

    if (totalPages > 1) {
      // Fetch remaining pages in parallel
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

      } catch (Exception e) {
        log.error("Error fetching movies in parallel", e);
        throw new RuntimeException("Failed to fetch all movies", e);
      }
    }

    long endTime = System.currentTimeMillis();
    log.info(
        "Completed fetching {} movies from {} pages in {} ms",
        allMovies.size(),
        totalPages,
        (endTime - startTime));

    return allMovies;
  }
}
