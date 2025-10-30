package com.challenge.movies.infrastructure.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.challenge.movies.application.service.DirectorService;
import com.challenge.movies.domain.exception.InvalidParameterException;
import com.challenge.movies.infrastructure.controller.dto.DirectorsResponseDto;
import com.challenge.movies.infrastructure.controller.dto.ErrorResponseDto;
import com.challenge.movies.infrastructure.controller.mapper.DirectorMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/directors")
@RequiredArgsConstructor
@Tag(name = "Directors", description = "Directors API to filter by movie count threshold")
public class DirectorController {

  private final DirectorService directorService;
  private final DirectorMapper directorMapper;

  @Operation(
      summary = "Get directors by threshold",
      description =
          "Retrieves a list of director names who have directed more than the specified threshold number of movies. "
              + "The list is returned in alphabetical order.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved the list of directors",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DirectorsResponseDto.class))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid threshold parameter (non-numeric or negative value)",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDto.class))),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ErrorResponseDto.class)))
      })
  @GetMapping
  public ResponseEntity<DirectorsResponseDto> getDirectorsByThreshold(
      @Parameter(
              description =
                  "Minimum number of movies directed (must be greater than or equal to 0)",
              required = true,
              example = "4")
          @RequestParam
          Integer threshold) {
    if (threshold == null || threshold < 0) {
      throw new InvalidParameterException("Threshold must be a non-negative integer");
    }
    List<String> directors = directorService.getDirectorsByThreshold(threshold);
    DirectorsResponseDto response = directorMapper.toDirectorsResponseDto(directors);
    return ResponseEntity.ok(response);
  }
}
