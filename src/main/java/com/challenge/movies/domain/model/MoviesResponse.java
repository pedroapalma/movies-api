package com.challenge.movies.domain.model;

import java.util.List;

public record MoviesResponse(
    Integer page, Integer perPage, Integer total, Integer totalPages, List<Movie> data) {}
