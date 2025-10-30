package com.challenge.movies.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.challenge.movies.infrastructure.client.dto.MoviesResponseDto;

@FeignClient(name = "movies-client", url = "${movies.api.url}")
public interface MoviesClient {

  @GetMapping("/api/movies/search")
  MoviesResponseDto getMovies(@RequestParam("page") Integer page);
}
