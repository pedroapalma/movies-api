package com.challenge.movies.infrastructure.controller.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(
    int status, String error, String message, String path, LocalDateTime timestamp) {}
