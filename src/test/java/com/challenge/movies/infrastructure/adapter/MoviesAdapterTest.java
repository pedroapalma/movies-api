package com.challenge.movies.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.challenge.movies.domain.model.MoviesResponse;
import com.challenge.movies.infrastructure.client.MoviesClient;
import com.challenge.movies.infrastructure.client.dto.MoviesResponseDto;
import com.challenge.movies.infrastructure.client.mapper.MoviesMapper;

@ExtendWith(MockitoExtension.class)
class MoviesAdapterTest {

  private static final Integer PAGE_1 = 1;

  @Mock private MoviesClient moviesClient;

  @Mock private MoviesMapper moviesMapper;

  @InjectMocks private MoviesAdapter moviesAdapter;

  @Test
  void returnMoviesResponseWhenGetMoviesByPage() {
    // Given
    MoviesResponseDto responseDto = mock(MoviesResponseDto.class);
    MoviesResponse expectedResponse = mock(MoviesResponse.class);

    when(moviesClient.getMovies(PAGE_1)).thenReturn(responseDto);
    when(moviesMapper.toMoviesResponse(responseDto)).thenReturn(expectedResponse);

    // When
    MoviesResponse result = moviesAdapter.getMoviesByPage(PAGE_1);

    // Then
    assertThat(result).isEqualTo(expectedResponse);
    verify(moviesClient, times(1)).getMovies(PAGE_1);
    verify(moviesMapper, times(1)).toMoviesResponse(responseDto);
  }

  @Test
  void throwExceptionWhenMoviesClientFails() {
    // Given
    when(moviesClient.getMovies(PAGE_1)).thenThrow(new RuntimeException("External API error"));

    // When / Then
    assertThatThrownBy(() -> moviesAdapter.getMoviesByPage(PAGE_1))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("External API error");

    verify(moviesClient, times(1)).getMovies(PAGE_1);
    verify(moviesMapper, times(0)).toMoviesResponse(null);
  }

  @Test
  void throwExceptionWhenMoviesMapperFails() {
    // Given
    MoviesResponseDto responseDto = mock(MoviesResponseDto.class);

    when(moviesClient.getMovies(PAGE_1)).thenReturn(responseDto);
    when(moviesMapper.toMoviesResponse(responseDto))
        .thenThrow(new RuntimeException("Mapping error"));

    // When / Then
    assertThatThrownBy(() -> moviesAdapter.getMoviesByPage(PAGE_1))
        .isInstanceOf(RuntimeException.class)
        .hasMessage("Mapping error");

    verify(moviesClient, times(1)).getMovies(PAGE_1);
    verify(moviesMapper, times(1)).toMoviesResponse(responseDto);
  }
}
