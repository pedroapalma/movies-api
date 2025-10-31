package com.challenge.movies.infrastructure.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.bucket4j.Bucket;

@ExtendWith(MockitoExtension.class)
class RateLimitInterceptorTest {

  private static final int CAPACITY = 10;
  private static final int DURATION_MINUTES = 1;
  private static final String REQUEST_URI = "/api/directors";

  private RateLimitInterceptor rateLimitInterceptor;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Object handler;

  @BeforeEach
  void setUp() {
    rateLimitInterceptor = new RateLimitInterceptor(CAPACITY, DURATION_MINUTES);
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    handler = mock(Object.class);
  }

  @Test
  void returnTrueWhenRateLimitNotExceeded() throws Exception {
    // When
    boolean result = rateLimitInterceptor.preHandle(request, response, handler);

    // Then
    assertThat(result).isTrue();
  }

  @Test
  void returnFalseWhenRateLimitExceeded() throws Exception {
    // Given
    Bucket bucket = (Bucket) ReflectionTestUtils.getField(rateLimitInterceptor, "bucket");
    bucket.tryConsume(CAPACITY);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    when(request.getRequestURI()).thenReturn(REQUEST_URI);
    when(response.getWriter()).thenReturn(printWriter);

    // When
    boolean result = rateLimitInterceptor.preHandle(request, response, handler);

    // Then
    assertThat(result).isFalse();
    verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    verify(response, times(1)).setContentType("application/json");
    verify(response, times(1)).getWriter();

    String responseBody = stringWriter.toString();
    assertThat(responseBody)
        .contains("\"status\":429")
        .contains("\"error\":\"Too Many Requests\"")
        .contains("\"message\":\"Rate limit exceeded. Please try again later.\"");
  }

  @Test
  void consumeTokenWhenRequestIsAllowed() throws Exception {
    // Given
    Bucket bucket = (Bucket) ReflectionTestUtils.getField(rateLimitInterceptor, "bucket");
    long availableTokensBefore = bucket.getAvailableTokens();

    // When
    boolean result = rateLimitInterceptor.preHandle(request, response, handler);

    // Then
    assertThat(result).isTrue();
    long availableTokensAfter = bucket.getAvailableTokens();
    assertThat(availableTokensAfter).isEqualTo(availableTokensBefore - 1);
  }

  @Test
  void allowMultipleRequestsUpToCapacity() throws Exception {
    // When / Then
    for (int i = 0; i < CAPACITY; i++) {
      boolean result = rateLimitInterceptor.preHandle(request, response, handler);
      assertThat(result).isTrue();
    }
  }

  @Test
  void rejectRequestAfterCapacityExceeded() throws Exception {
    // Given
    for (int i = 0; i < CAPACITY; i++) {
      rateLimitInterceptor.preHandle(request, response, handler);
    }

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    when(request.getRequestURI()).thenReturn(REQUEST_URI);
    when(response.getWriter()).thenReturn(printWriter);

    // When
    boolean result = rateLimitInterceptor.preHandle(request, response, handler);

    // Then
    assertThat(result).isFalse();
    verify(response, times(1)).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
  }

  @Test
  void initializeBucketWithCustomCapacity() {
    // Given
    int customCapacity = 5;
    int customDuration = 2;

    // When
    RateLimitInterceptor customInterceptor =
        new RateLimitInterceptor(customCapacity, customDuration);

    // Then
    Bucket bucket = (Bucket) ReflectionTestUtils.getField(customInterceptor, "bucket");
    assertThat(bucket.getAvailableTokens()).isEqualTo(customCapacity);
  }

  @Test
  void setCorrectHttpStatusAndContentTypeWhenRateLimitExceeded() throws Exception {
    // Given
    Bucket bucket = (Bucket) ReflectionTestUtils.getField(rateLimitInterceptor, "bucket");
    bucket.tryConsume(CAPACITY);

    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);

    when(request.getRequestURI()).thenReturn(REQUEST_URI);
    when(response.getWriter()).thenReturn(printWriter);

    // When
    rateLimitInterceptor.preHandle(request, response, handler);

    // Then
    verify(response, times(1)).setStatus(429);
    verify(response, times(1)).setContentType("application/json");
  }
}
