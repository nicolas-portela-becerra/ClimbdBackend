package com.climbingapp.api.controller;

import com.climbingapp.api.dto.AuthResponse;
import com.climbingapp.api.dto.LoginRequest;
import com.climbingapp.api.dto.OAuthRequest;
import com.climbingapp.api.dto.RefreshRequest;
import com.climbingapp.api.dto.RegisterRequest;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.UserUseCase;
import com.climbingapp.jwt.utils.JwtUserDetailsService;
import com.climbingapp.jwt.utils.RefreshTokenService;
import com.climbingapp.jwt.utils.TokenManager;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class AuthController implements AuthApi {

    @Autowired private AuthenticationManager authenticationManager;

    @Autowired private JwtUserDetailsService jwtUserDetailsService;

    @Autowired private TokenManager tokenManager;

    @Autowired private RefreshTokenService refreshTokenService;

    @Autowired private UserRepository userRepository;

    @Autowired private UserUseCase userUseCase;

    @Autowired private ApiDtoMapper mapper;

    @Override
    public ResponseEntity<AuthResponse> authLogin(LoginRequest loginRequest) {
        String email = normalize(loginRequest.getEmail());
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String accessToken = tokenManager.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.issue(userDetails, null);
        UserDTO user = userRepository.findByEmail(email);
        log.info("Login successful for user {}", email);
        return ResponseEntity.ok(buildAuthResponse(accessToken, refreshToken, user));
    }

    @Override
    public ResponseEntity<AuthResponse> authRegister(RegisterRequest registerRequest) {
        UserDTO savedUser =
                userUseCase.register(
                        registerRequest.getName(),
                        registerRequest.getEmail(),
                        registerRequest.getPassword());
        UserCredentials credentials = userRepository.findCredentialsByEmail(savedUser.getEmail());
        UserDetails userDetails = buildUserDetails(credentials);
        String accessToken = tokenManager.generateAccessToken(userDetails);
        String refreshToken = refreshTokenService.issue(userDetails, null);
        log.info("User registered: {}", savedUser.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildAuthResponse(accessToken, refreshToken, savedUser));
    }

    @Override
    public ResponseEntity<AuthResponse> authRefresh(RefreshRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            Claims claims = tokenManager.getPayload(refreshToken);
            String username = claims.getSubject();
            UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(normalize(username));
            if (!userDetails.isEnabled()
                    || !tokenManager.validateRefreshToken(claims, userDetails)) {
                log.warn("Refresh rejected for deactivated or invalid user {}", username);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String jti = claims.getId();
            String familyId = claims.get(TokenManager.FAMILY_CLAIM, String.class);
            if (jti == null || !refreshTokenService.consume(jti)) {
                if (familyId != null) {
                    refreshTokenService.revokeFamily(familyId);
                    log.warn(
                            "Refresh token reuse detected for user {} — family {} revoked",
                            username,
                            familyId);
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String newAccessToken = tokenManager.generateAccessToken(userDetails);
            String newRefreshToken = refreshTokenService.issue(userDetails, familyId);
            UserDTO user = userRepository.findByEmail(normalize(username));
            log.info("Refresh token rotated for user {}", username);
            return ResponseEntity.ok(buildAuthResponse(newAccessToken, newRefreshToken, user));
        } catch (JwtException e) {
            log.error("Refreshing JWT Token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @Override
    public ResponseEntity<Void> authLogout(RefreshRequest refreshRequest) {
        try {
            Claims claims = tokenManager.getPayload(refreshRequest.getRefreshToken());
            String jti = claims.getId();
            if (jti != null) {
                refreshTokenService.consume(jti);
                log.info("Logout: refresh token revoked for user {}", claims.getSubject());
            }
        } catch (JwtException e) {
            log.debug("Logout called with invalid token: {}", e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<Void> authLogoutAll() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        refreshTokenService.revokeAllForUser(email);
        log.info("Logout-all: all sessions revoked for user {}", email);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<AuthResponse> authOauthGoogle(OAuthRequest oauthRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @Override
    public ResponseEntity<AuthResponse> authOauthApple(OAuthRequest oauthRequest) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    private UserDetails buildUserDetails(UserCredentials credentials) {
        return new User(
                credentials.email(),
                credentials.passwordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + credentials.role())));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private AuthResponse buildAuthResponse(String accessToken, String refreshToken, UserDTO user) {
        return new AuthResponse()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(mapper.toUserDto(user));
    }
}
