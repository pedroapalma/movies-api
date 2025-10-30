package com.challenge.movies.domain.port;

import com.challenge.movies.domain.model.MoviesResponse;

public interface MoviesPort {

  MoviesResponse getMoviesByPage(Integer page);
}
