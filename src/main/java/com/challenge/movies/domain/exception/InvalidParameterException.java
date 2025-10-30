package com.challenge.movies.domain.exception;

public class InvalidParameterException extends RuntimeException {

  public InvalidParameterException(String message) {
    super(message);
  }
}
