package com.challenge.movies.infrastructure.client.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MoviesResponseDto(
    @JsonProperty("page") Integer page,
    @JsonProperty("per_page") Integer perPage,
    @JsonProperty("total") Integer total,
    @JsonProperty("total_pages") Integer totalPages,
    @JsonProperty("data") List<MovieDto> data) {}
