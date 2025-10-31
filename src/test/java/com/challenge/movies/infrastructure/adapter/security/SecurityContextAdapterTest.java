package com.challenge.movies.infrastructure.adapter.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class SecurityContextAdapterTest {

  private static final String CLIENT_ID = "test-client";
  private static final String AZP_CLAIM = "azp";
  private static final String CLIENT_ID_CLAIM = "client_id";
  private static final String SCOPE_CLAIM = "scope";
  private static final String ANONYMOUS_USER = "anonymousUser";

  @InjectMocks private SecurityContextAdapter securityContextAdapter;

  @Test
  void returnClientIdFromAzpClaimWhenJwtAuthentication() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    JwtAuthenticationToken jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
    Jwt jwt = mock(Jwt.class);

    when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsString(AZP_CLAIM)).thenReturn(CLIENT_ID);
    when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      Optional<String> result = securityContextAdapter.getCurrentClientId();

      // Then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(CLIENT_ID);
    }
  }

  @Test
  void returnClientIdFromClientIdClaimWhenAzpIsNull() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    JwtAuthenticationToken jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
    Jwt jwt = mock(Jwt.class);

    when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsString(AZP_CLAIM)).thenReturn(null);
    when(jwt.getClaimAsString(CLIENT_ID_CLAIM)).thenReturn(CLIENT_ID);
    when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      Optional<String> result = securityContextAdapter.getCurrentClientId();

      // Then
      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo(CLIENT_ID);
    }
  }

  @Test
  void returnEmptyOptionalWhenNotJwtAuthentication() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      Optional<String> result = securityContextAdapter.getCurrentClientId();

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Test
  void returnEmptyOptionalWhenAuthenticationIsNull() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      Optional<String> result = securityContextAdapter.getCurrentClientId();

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Test
  void returnEmptyOptionalWhenGetCurrentClientIdThrowsException() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Security error"));

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      Optional<String> result = securityContextAdapter.getCurrentClientId();

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Test
  void returnScopesWhenJwtAuthentication() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    JwtAuthenticationToken jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
    Jwt jwt = mock(Jwt.class);
    List<String> scopes = List.of("read", "write");

    when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsStringList(SCOPE_CLAIM)).thenReturn(scopes);
    when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      List<String> result = securityContextAdapter.getCurrentScopes();

      // Then
      assertThat(result).containsExactly("read", "write");
    }
  }

  @Test
  void returnEmptyListWhenScopesIsNull() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    JwtAuthenticationToken jwtAuthenticationToken = mock(JwtAuthenticationToken.class);
    Jwt jwt = mock(Jwt.class);

    when(jwtAuthenticationToken.getToken()).thenReturn(jwt);
    when(jwt.getClaimAsStringList(SCOPE_CLAIM)).thenReturn(null);
    when(securityContext.getAuthentication()).thenReturn(jwtAuthenticationToken);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      List<String> result = securityContextAdapter.getCurrentScopes();

      // Then
      assertThat(result).isEqualTo(Collections.emptyList());
    }
  }

  @Test
  void returnEmptyListWhenNotJwtAuthentication() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      List<String> result = securityContextAdapter.getCurrentScopes();

      // Then
      assertThat(result).isEqualTo(Collections.emptyList());
    }
  }

  @Test
  void returnEmptyListWhenAuthenticationIsNull() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      List<String> result = securityContextAdapter.getCurrentScopes();

      // Then
      assertThat(result).isEqualTo(Collections.emptyList());
    }
  }

  @Test
  void returnEmptyListWhenGetCurrentScopesThrowsException() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Security error"));

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      List<String> result = securityContextAdapter.getCurrentScopes();

      // Then
      assertThat(result).isEqualTo(Collections.emptyList());
    }
  }

  @Test
  void returnTrueWhenAuthenticatedAndNotAnonymous() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn("user-principal");

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      boolean result = securityContextAdapter.isAuthenticated();

      // Then
      assertThat(result).isTrue();
    }
  }

  @Test
  void returnFalseWhenAuthenticationIsNull() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenReturn(null);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      boolean result = securityContextAdapter.isAuthenticated();

      // Then
      assertThat(result).isFalse();
    }
  }

  @Test
  void returnFalseWhenNotAuthenticated() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(false);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      boolean result = securityContextAdapter.isAuthenticated();

      // Then
      assertThat(result).isFalse();
    }
  }

  @Test
  void returnFalseWhenPrincipalIsAnonymousUser() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.isAuthenticated()).thenReturn(true);
    when(authentication.getPrincipal()).thenReturn(ANONYMOUS_USER);

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      boolean result = securityContextAdapter.isAuthenticated();

      // Then
      assertThat(result).isFalse();
    }
  }

  @Test
  void returnFalseWhenIsAuthenticatedThrowsException() {
    // Given
    SecurityContext securityContext = mock(SecurityContext.class);

    when(securityContext.getAuthentication()).thenThrow(new RuntimeException("Security error"));

    try (MockedStatic<SecurityContextHolder> mockedStatic =
        mockStatic(SecurityContextHolder.class)) {
      mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);

      // When
      boolean result = securityContextAdapter.isAuthenticated();

      // Then
      assertThat(result).isFalse();
    }
  }
}
