package com.climbingapp.jwt.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.crypto.SecretKey;

@Component
public class TokenManager {

    public static final long ACCESS_TOKEN_VALIDITY = 15 * 60L;
    public static final long REFRESH_TOKEN_VALIDITY = 30L * 24 * 60 * 60L;

    public static final String ISSUER = "climbing-app";
    public static final String TYPE_CLAIM = "type";
    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";
    public static final String FAMILY_CLAIM = "fam";

    @Value("${spring.application.secret}")
    private String secret;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TYPE_CLAIM, TYPE_ACCESS);

        return Jwts.builder()
                .claims(claims)
                .subject(userDetails.getUsername())
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALIDITY * 1000))
                .signWith(getKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, String jti, String familyId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TYPE_CLAIM, TYPE_REFRESH);
        claims.put(FAMILY_CLAIM, familyId);

        return Jwts.builder()
                .claims(claims)
                .id(jti)
                .subject(userDetails.getUsername())
                .issuer(ISSUER)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY * 1000))
                .signWith(getKey())
                .compact();
    }

    public boolean validateAccessToken(String token, UserDetails userDetails) {
        Claims claims = getPayload(token);
        String username = claims.getSubject();
        String type = claims.get(TYPE_CLAIM, String.class);
        boolean isTokenExpired = claims.getExpiration().before(new Date());
        return TYPE_ACCESS.equals(type)
                && Objects.equals(username, userDetails.getUsername())
                && !isTokenExpired;
    }

    public boolean validateRefreshToken(Claims claims, UserDetails userDetails) {
        String type = claims.get(TYPE_CLAIM, String.class);
        String username = claims.getSubject();
        boolean isTokenExpired = claims.getExpiration().before(new Date());
        return TYPE_REFRESH.equals(type)
                && Objects.equals(username, userDetails.getUsername())
                && !isTokenExpired;
    }

    public Claims getPayload(String token) {
        return Jwts.parser()
                .verifyWith((SecretKey) getKey())
                .requireIssuer(ISSUER)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Key getKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
