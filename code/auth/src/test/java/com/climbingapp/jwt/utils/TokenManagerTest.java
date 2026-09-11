package com.climbingapp.jwt.utils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;
import java.util.Date;
import java.util.List;

import javax.crypto.SecretKey;

class TokenManagerTest {

    private static final String SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    private TokenManager tokenManager;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        tokenManager = new TokenManager();
        ReflectionTestUtils.setField(tokenManager, "secret", SECRET);
        userDetails =
                new User("user@test.com", "hash", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void accessTokenIsValidForSameUser() {
        String token = tokenManager.generateAccessToken(userDetails);
        assertTrue(tokenManager.validateAccessToken(token, userDetails));
    }

    @Test
    void refreshTokenIsRejectedAsAccessToken() {
        // Regression: token-type confusion — a long-lived refresh token must never
        // authenticate a request as an access token
        String refreshToken = tokenManager.generateRefreshToken(userDetails, "jti-1", "fam-1");
        assertFalse(tokenManager.validateAccessToken(refreshToken, userDetails));
    }

    @Test
    void accessTokenIsRejectedAsRefreshToken() {
        String accessToken = tokenManager.generateAccessToken(userDetails);
        assertFalse(
                tokenManager.validateRefreshToken(
                        tokenManager.getPayload(accessToken), userDetails));
    }

    @Test
    void accessTokenIsRejectedForDifferentUser() {
        String token = tokenManager.generateAccessToken(userDetails);
        UserDetails other = new User("other@test.com", "hash", List.of());
        assertFalse(tokenManager.validateAccessToken(token, other));
    }

    @Test
    void tokenWithoutIssuerIsRejected() {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
        String foreignToken =
                Jwts.builder()
                        .subject("user@test.com")
                        .issuedAt(new Date())
                        .expiration(new Date(System.currentTimeMillis() + 60_000))
                        .signWith(key)
                        .compact();
        assertThrows(JwtException.class, () -> tokenManager.getPayload(foreignToken));
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = tokenManager.generateAccessToken(userDetails);
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertThrows(JwtException.class, () -> tokenManager.getPayload(tampered));
    }
}
