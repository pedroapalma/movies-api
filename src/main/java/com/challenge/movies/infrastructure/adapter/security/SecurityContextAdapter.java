package com.challenge.movies.infrastructure.adapter.security;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.challenge.movies.domain.port.SecurityPort;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class SecurityContextAdapter implements SecurityPort {

  @Override
  public Optional<String> getCurrentClientId() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Jwt jwt = jwtAuth.getToken();
        String clientId = jwt.getClaimAsString("azp");
        if (clientId == null) {
          clientId = jwt.getClaimAsString("client_id");
        }
        return Optional.ofNullable(clientId);
      }

      return Optional.empty();
    } catch (Exception e) {
      log.warn("Error retrieving client ID from security context", e);
      return Optional.empty();
    }
  }

  @Override
  public List<String> getCurrentScopes() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

      if (authentication instanceof JwtAuthenticationToken jwtAuth) {
        Jwt jwt = jwtAuth.getToken();
        List<String> scopes = jwt.getClaimAsStringList("scope");
        return scopes != null ? scopes : Collections.emptyList();
      }

      return Collections.emptyList();
    } catch (Exception e) {
      log.warn("Error retrieving scopes from security context", e);
      return Collections.emptyList();
    }
  }

  @Override
  public boolean isAuthenticated() {
    try {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      return authentication != null
          && authentication.isAuthenticated()
          && !"anonymousUser".equals(authentication.getPrincipal());
    } catch (Exception e) {
      log.warn("Error checking authentication status", e);
      return false;
    }
  }
}
