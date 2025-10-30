package com.challenge.movies.domain.model;

public record Movie(
    String title,
    Integer year,
    String rated,
    String released,
    String runtime,
    String genre,
    String director,
    String writer,
    String actors) {}
