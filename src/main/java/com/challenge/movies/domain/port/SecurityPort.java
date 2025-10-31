package com.challenge.movies.domain.port;

import java.util.List;
import java.util.Optional;

public interface SecurityPort {

  Optional<String> getCurrentClientId();

  List<String> getCurrentScopes();

  boolean isAuthenticated();
}
