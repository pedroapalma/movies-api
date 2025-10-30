package com.challenge.movies.infrastructure.adapter;

import org.springframework.stereotype.Component;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.MoviesPort;
import com.challenge.movies.infrastructure.client.MoviesClient;
import com.challenge.movies.infrastructure.client.dto.MoviesResponseDto;
import com.challenge.movies.infrastructure.client.mapper.MoviesMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MoviesAdapter implements MoviesPort {

  private final MoviesClient moviesClient;
  private final MoviesMapper moviesMapper;

  @Override
  public MoviesResponse getMoviesByPage(Integer page) {
    MoviesResponseDto responseDto = moviesClient.getMovies(page);
    return moviesMapper.toMoviesResponse(responseDto);
  }
}
