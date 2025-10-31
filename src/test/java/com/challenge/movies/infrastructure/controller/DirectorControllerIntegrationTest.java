package com.challenge.movies.infrastructure.controller;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.domain.port.CachePort;
import com.challenge.movies.infrastructure.client.MoviesClient;
import com.challenge.movies.infrastructure.client.dto.MoviesResponseDto;
import com.challenge.movies.infrastructure.client.mapper.MoviesMapper;
import com.challenge.movies.utils.DummyData;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DirectorControllerIntegrationTest {

  private static final String API_DIRECTORS_PATH = "/api/directors";

  @Autowired private MockMvc mockMvc;

  @MockitoBean private MoviesClient moviesClient;

  @Autowired private MoviesMapper moviesMapper;

  @Autowired private CachePort cachePort;

  @AfterEach
  void tearDown() {
    cachePort.clear();
  }

  @Test
  void returnDirectorsWhenThresholdIs4() throws Exception {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    MoviesResponseDto dto1 = moviesMapper.toMoviesResponseDto(page1);
    MoviesResponseDto dto2 = moviesMapper.toMoviesResponseDto(page2);
    MoviesResponseDto dto3 = moviesMapper.toMoviesResponseDto(page3);

    when(moviesClient.getMovies(1)).thenReturn(dto1);
    when(moviesClient.getMovies(2)).thenReturn(dto2);
    when(moviesClient.getMovies(3)).thenReturn(dto3);

    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH).param("threshold", "4").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directors", hasSize(2)))
        .andExpect(jsonPath("$.directors[0]", is("Martin Scorsese")))
        .andExpect(jsonPath("$.directors[1]", is("Woody Allen")));
  }

  @Test
  void returnAllDirectorsWhenThresholdIs0() throws Exception {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    MoviesResponseDto dto1 = moviesMapper.toMoviesResponseDto(page1);
    MoviesResponseDto dto2 = moviesMapper.toMoviesResponseDto(page2);
    MoviesResponseDto dto3 = moviesMapper.toMoviesResponseDto(page3);

    when(moviesClient.getMovies(1)).thenReturn(dto1);
    when(moviesClient.getMovies(2)).thenReturn(dto2);
    when(moviesClient.getMovies(3)).thenReturn(dto3);

    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH).param("threshold", "0").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directors", hasSize(7)))
        .andExpect(
            jsonPath(
                "$.directors",
                containsInAnyOrder(
                    "Clint Eastwood",
                    "Juan José Campanella",
                    "M. Night Shyamalan",
                    "Martin Scorsese",
                    "Pedro Almodóvar",
                    "Quentin Tarantino",
                    "Woody Allen")));
  }

  @Test
  void returnEmptyListWhenThresholdIs10() throws Exception {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    MoviesResponseDto dto1 = moviesMapper.toMoviesResponseDto(page1);
    MoviesResponseDto dto2 = moviesMapper.toMoviesResponseDto(page2);
    MoviesResponseDto dto3 = moviesMapper.toMoviesResponseDto(page3);

    when(moviesClient.getMovies(1)).thenReturn(dto1);
    when(moviesClient.getMovies(2)).thenReturn(dto2);
    when(moviesClient.getMovies(3)).thenReturn(dto3);

    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH)
                .param("threshold", "10")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directors", hasSize(0)));
  }

  @Test
  void returnBadRequestWhenThresholdIsNegative() throws Exception {
    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH)
                .param("threshold", "-1")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")))
        .andExpect(jsonPath("$.message", is("Threshold must be a non-negative integer")))
        .andExpect(jsonPath("$.path", is(API_DIRECTORS_PATH)));
  }

  @Test
  void returnBadRequestWhenThresholdIsMissing() throws Exception {
    // When / Then
    mockMvc
        .perform(get(API_DIRECTORS_PATH).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")))
        .andExpect(jsonPath("$.path", is(API_DIRECTORS_PATH)));
  }

  @Test
  void returnBadRequestWhenThresholdIsNotNumeric() throws Exception {
    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH)
                .param("threshold", "invalid")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status", is(400)))
        .andExpect(jsonPath("$.error", is("Bad Request")))
        .andExpect(jsonPath("$.path", is(API_DIRECTORS_PATH)));
  }

  @Test
  void returnInternalServerErrorWhenServiceThrowsException() throws Exception {
    // Given
    when(moviesClient.getMovies(anyInt())).thenThrow(new RuntimeException("External API error"));

    // When / Then
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH).param("threshold", "4").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.status", is(500)))
        .andExpect(jsonPath("$.error", is("Internal Server Error")))
        .andExpect(
            jsonPath("$.message", is("An unexpected error occurred. Please try again later.")))
        .andExpect(jsonPath("$.path", is(API_DIRECTORS_PATH)));
  }

  @Test
  void returnDirectorsWithCachedResults() throws Exception {
    // Given
    MoviesResponse page1 = DummyData.deserialize(DummyData.MOVIES_PAGE_1, MoviesResponse.class);
    MoviesResponse page2 = DummyData.deserialize(DummyData.MOVIES_PAGE_2, MoviesResponse.class);
    MoviesResponse page3 = DummyData.deserialize(DummyData.MOVIES_PAGE_3, MoviesResponse.class);

    MoviesResponseDto dto1 = moviesMapper.toMoviesResponseDto(page1);
    MoviesResponseDto dto2 = moviesMapper.toMoviesResponseDto(page2);
    MoviesResponseDto dto3 = moviesMapper.toMoviesResponseDto(page3);

    when(moviesClient.getMovies(1)).thenReturn(dto1);
    when(moviesClient.getMovies(2)).thenReturn(dto2);
    when(moviesClient.getMovies(3)).thenReturn(dto3);

    // First request - cache miss
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH).param("threshold", "4").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directors", hasSize(2)));

    // Second request - should use cache for page 1
    mockMvc
        .perform(
            get(API_DIRECTORS_PATH).param("threshold", "4").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.directors", hasSize(2)))
        .andExpect(jsonPath("$.directors[0]", is("Martin Scorsese")))
        .andExpect(jsonPath("$.directors[1]", is("Woody Allen")));
  }
}
