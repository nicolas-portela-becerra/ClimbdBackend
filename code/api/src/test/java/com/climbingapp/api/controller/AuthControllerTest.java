package com.climbingapp.api.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.api.dto.AuthResponse;
import com.climbingapp.api.dto.LoginRequest;
import com.climbingapp.api.dto.RefreshRequest;
import com.climbingapp.api.dto.RegisterRequest;
import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.UserUseCase;
import com.climbingapp.jwt.utils.JwtUserDetailsService;
import com.climbingapp.jwt.utils.RefreshTokenService;
import com.climbingapp.jwt.utils.TokenManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;

    @Mock private JwtUserDetailsService jwtUserDetailsService;

    @Mock private TokenManager tokenManager;

    @Mock private RefreshTokenService refreshTokenService;

    @Mock private UserRepository userRepository;

    @Mock private UserUseCase userUseCase;

    @InjectMocks private AuthController controller;

    private UserDetails userDetails;

    private UserDTO userDto;

    @BeforeEach
    void setUp() {
        userDetails =
                new User("user@test.com", "hash", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        userDto =
                new UserDTO(
                        1,
                        "user@test.com",
                        "Test",
                        "USER",
                        "LOCAL",
                        null,
                        LocalDateTime.now(),
                        null);
    }

    @Test
    void loginReusesAuthenticationPrincipalWithoutSecondLookup() {
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenManager.generateAccessToken(userDetails)).thenReturn("access-token");
        when(refreshTokenService.issue(userDetails, null)).thenReturn("refresh-token");
        when(userRepository.findByEmail("user@test.com")).thenReturn(userDto);

        ResponseEntity<AuthResponse> response =
                controller.authLogin(new LoginRequest().email("User@Test.com").password("secret"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access-token", response.getBody().getAccessToken());
        assertEquals("refresh-token", response.getBody().getRefreshToken());
        // Email normalized + no redundant DB reload of the user we just authenticated
        verify(userRepository).findByEmail("user@test.com");
        verify(jwtUserDetailsService, never()).loadUserByUsername(anyString());
    }

    @Test
    void registerDelegatesToUserUseCaseAndReturnsCreatedWithTokens() {
        when(userUseCase.register("Test", "user@test.com", "password123")).thenReturn(userDto);
        when(userRepository.findCredentialsByEmail("user@test.com"))
                .thenReturn(new UserCredentials(1, "user@test.com", "hash", "USER"));
        when(tokenManager.generateAccessToken(any(UserDetails.class))).thenReturn("access-token");
        when(refreshTokenService.issue(any(UserDetails.class), isNull()))
                .thenReturn("refresh-token");

        ResponseEntity<AuthResponse> response =
                controller.authRegister(
                        new RegisterRequest()
                                .email("user@test.com")
                                .name("Test")
                                .password("password123"));

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("access-token", response.getBody().getAccessToken());
        assertEquals("refresh-token", response.getBody().getRefreshToken());
        assertNotNull(response.getBody().getUser());
        assertEquals("user@test.com", response.getBody().getUser().getEmail());
    }

    @Test
    void registerConflictFromUseCasePropagatesWithoutIssuingTokens() {
        when(userUseCase.register(any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("Duplicate registration"));

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        controller.authRegister(
                                new RegisterRequest()
                                        .email("user@test.com")
                                        .name("Test")
                                        .password("password123")));

        verify(tokenManager, never()).generateAccessToken(any());
    }

    @Test
    void refreshRotatesTheRefreshToken() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.getId()).thenReturn("jti-1");
        when(claims.get(TokenManager.FAMILY_CLAIM, String.class)).thenReturn("fam-1");
        when(tokenManager.getPayload("old-token")).thenReturn(claims);
        when(jwtUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(tokenManager.validateRefreshToken(claims, userDetails)).thenReturn(true);
        when(refreshTokenService.consume("jti-1")).thenReturn(true);
        when(tokenManager.generateAccessToken(userDetails)).thenReturn("new-access");
        when(refreshTokenService.issue(userDetails, "fam-1")).thenReturn("new-refresh");
        when(userRepository.findByEmail("user@test.com")).thenReturn(userDto);

        ResponseEntity<AuthResponse> response =
                controller.authRefresh(new RefreshRequest().refreshToken("old-token"));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("new-access", response.getBody().getAccessToken());
        // Rotation: the returned refresh token must NOT be the one that was presented
        assertEquals("new-refresh", response.getBody().getRefreshToken());
    }

    @Test
    void refreshWithAlreadyConsumedTokenRevokesWholeFamily() {
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.getId()).thenReturn("jti-1");
        when(claims.get(TokenManager.FAMILY_CLAIM, String.class)).thenReturn("fam-1");
        when(tokenManager.getPayload("replayed-token")).thenReturn(claims);
        when(jwtUserDetailsService.loadUserByUsername("user@test.com")).thenReturn(userDetails);
        when(tokenManager.validateRefreshToken(claims, userDetails)).thenReturn(true);
        when(refreshTokenService.consume("jti-1")).thenReturn(false);

        ResponseEntity<AuthResponse> response =
                controller.authRefresh(new RefreshRequest().refreshToken("replayed-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(refreshTokenService).revokeFamily("fam-1");
        verify(tokenManager, never()).generateAccessToken(any());
    }

    @Test
    void refreshWithInvalidTokenReturns401() {
        when(tokenManager.getPayload("bad-token")).thenThrow(new JwtException("bad"));

        ResponseEntity<AuthResponse> response =
                controller.authRefresh(new RefreshRequest().refreshToken("bad-token"));

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
