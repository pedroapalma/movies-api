package com.challenge.movies.infrastructure.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.challenge.movies.domain.model.Movie;
import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.infrastructure.client.dto.MovieDto;
import com.challenge.movies.infrastructure.client.dto.MoviesResponseDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MoviesMapper {

  Movie toMovie(MovieDto movieDto);

  MoviesResponse toMoviesResponse(MoviesResponseDto moviesResponseDto);
}
