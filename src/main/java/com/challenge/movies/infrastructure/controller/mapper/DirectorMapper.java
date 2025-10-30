package com.challenge.movies.infrastructure.controller.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.challenge.movies.infrastructure.controller.dto.DirectorsResponseDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface DirectorMapper {

  default DirectorsResponseDto toDirectorsResponseDto(List<String> directors) {
    return new DirectorsResponseDto(directors);
  }
}
