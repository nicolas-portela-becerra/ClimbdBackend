package com.climbingapp.jwt.utils;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.jsonwebtoken.Claims;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

class JwtFilterTest {

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void tokenOfDeletedUserContinuesUnauthenticatedInsteadOfFailing() throws Exception {
        JwtUserDetailsService userDetailsService = mock(JwtUserDetailsService.class);
        TokenManager tokenManager = mock(TokenManager.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("deleted@x.com");
        when(tokenManager.getPayload("tok")).thenReturn(claims);
        when(userDetailsService.loadUserByUsername("deleted@x.com"))
                .thenThrow(new UsernameNotFoundException("gone"));

        JwtFilter filter = new JwtFilter();
        ReflectionTestUtils.setField(filter, "jwtUserDetailsService", userDetailsService);
        ReflectionTestUtils.setField(filter, "tokenManager", tokenManager);

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("Authorization")).thenReturn("Bearer tok");
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain chain = mock(FilterChain.class);

        filter.doFilterInternal(request, response, chain);

        verify(chain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
