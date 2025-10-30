package com.challenge.movies.infrastructure.interceptor;

import java.time.Duration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {

  private final Bucket bucket;

  public RateLimitInterceptor(
      @Value("${rate-limit.capacity:10}") int capacity,
      @Value("${rate-limit.duration-minutes:1}") int durationMinutes) {
    Bandwidth limit =
        Bandwidth.builder()
            .capacity(capacity)
            .refillIntervally(capacity, Duration.ofMinutes(durationMinutes))
            .build();
    this.bucket = Bucket.builder().addLimit(limit).build();
    log.info("Rate limiter initialized: {} requests per {} minute(s)", capacity, durationMinutes);
  }

  @Override
  public boolean preHandle(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull Object handler)
      throws Exception {

    if (bucket.tryConsume(1)) {
      return true;
    }

    // Rate limit exceeded
    log.warn("Rate limit exceeded for request: {}", request.getRequestURI());
    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
    response.setContentType("application/json");
    response
        .getWriter()
        .write(
            "{\"status\":429,\"error\":\"Too Many Requests\","
                + "\"message\":\"Rate limit exceeded. Please try again later.\"}");
    return false;
  }
}
