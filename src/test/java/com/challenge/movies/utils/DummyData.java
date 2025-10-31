package com.challenge.movies.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;

public interface DummyData {

  String MOVIE = "movie.json";

  String MOVIES_PAGE_1 = "movies_paged_1.json";

  String MOVIES_PAGE_2 = "movies_paged_2.json";

  String MOVIES_PAGE_3 = "movies_paged_3.json";

  ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  static <T> T deserialize(String resourcePath, Class<T> clazz) {
    try {
      OBJECT_MAPPER.setPropertyNamingStrategy(new PropertyNamingStrategies.SnakeCaseStrategy());
      return OBJECT_MAPPER.readValue(toJson(resourcePath), clazz);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  static String toJson(String path) {
    try {
      return new String(Files.readAllBytes(Paths.get("src/test/resources/" + path)));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
