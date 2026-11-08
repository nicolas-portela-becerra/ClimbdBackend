package com.climbingapp.boot.config.security;

import com.climbingapp.api.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bucket;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Limits how often each user can send competition results. Limited per user, not per IP:
 * participants usually share the gym's Wi-Fi and therefore its public IP.
 */
@Slf4j
@Component("resultSubmissionRateLimitFilter")
public class ResultSubmissionRateLimitFilter extends OncePerRequestFilter {

    private static final Pattern RESULTS_PATH = Pattern.compile("^/api/competitions/\\d+/results$");

    private static final int CAPACITY = 3;

    private static final Duration REFILL_PERIOD = Duration.ofSeconds(15);

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equalsIgnoreCase(request.getMethod())
                || !RESULTS_PATH.matcher(request.getRequestURI()).matches();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            // Unauthenticated requests are rejected later by Spring Security
            filterChain.doFilter(request, response);
            return;
        }

        String user = authentication.getName();
        Bucket bucket = buckets.computeIfAbsent(user, key -> newBucket());
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            log.warn("Result submission rate limit exceeded for user {}", user);
            response.setStatus(429);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorResponse body =
                    new ErrorResponse()
                            .message(
                                    "Results are being sent too often — try again in a few seconds")
                            .status(429);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        }
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(CAPACITY).refillGreedy(CAPACITY, REFILL_PERIOD))
                .build();
    }
}
