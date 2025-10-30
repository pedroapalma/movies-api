package com.challenge.movies.domain.port;

import java.util.Optional;

public interface CachePort {

  <T> Optional<T> get(String key, Class<T> type);

  <T> void put(String key, T value);

  void evict(String key);

  void clear();
}
