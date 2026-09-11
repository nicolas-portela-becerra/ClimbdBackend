package com.climbingapp.boot.config.security;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

class RateLimitFilterTest {

    private RateLimitFilter filter = new RateLimitFilter();

    @Test
    void allowsUpToTenRequestsPerMinuteThenRejectsWith429() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn("10.0.0.1");

        StringWriter body = new StringWriter();
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(body));

        FilterChain chain = mock(FilterChain.class);

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(request, response, chain);
        }
        verify(chain, times(10)).doFilter(request, response);

        filter.doFilterInternal(request, response, chain);

        // 11th request within the window is rejected, chain not invoked again
        verify(chain, times(10)).doFilter(request, response);
        verify(response).setStatus(429);
        assertTrue(body.toString().contains("Too many requests"));
    }

    @Test
    void separateClientsHaveSeparateBuckets() throws Exception {
        FilterChain chain = mock(FilterChain.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(new StringWriter()));

        for (int i = 0; i < 10; i++) {
            filter.doFilterInternal(requestFor("10.0.0.1"), response, chain);
        }
        // A different IP is still allowed after the first one exhausted its bucket
        filter.doFilterInternal(requestFor("10.0.0.2"), response, chain);

        verify(chain, times(11))
                .doFilter(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void doesNotFilterNonAuthEndpoints() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/gyms");

        assertTrue(filter.shouldNotFilter(request));
    }

    private HttpServletRequest requestFor(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn(ip);
        return request;
    }
}
