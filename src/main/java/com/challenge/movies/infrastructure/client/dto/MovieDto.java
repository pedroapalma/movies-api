package com.challenge.movies.infrastructure.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MovieDto(
    @JsonProperty("Title") String title,
    @JsonProperty("Year") Integer year,
    @JsonProperty("Rated") String rated,
    @JsonProperty("Released") String released,
    @JsonProperty("Runtime") String runtime,
    @JsonProperty("Genre") String genre,
    @JsonProperty("Director") String director,
    @JsonProperty("Writer") String writer,
    @JsonProperty("Actors") String actors) {}
