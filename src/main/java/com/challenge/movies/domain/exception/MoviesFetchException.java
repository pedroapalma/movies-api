package com.challenge.movies.domain.exception;

public class MoviesFetchException extends RuntimeException {

  public MoviesFetchException(String message, Throwable cause) {
    super(message, cause);
  }
}
