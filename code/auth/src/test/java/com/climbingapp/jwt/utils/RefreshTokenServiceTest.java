package com.climbingapp.jwt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private StringRedisTemplate redisTemplate;

    @Mock private HashOperations<String, Object, Object> hashOperations;

    @Mock private SetOperations<String, String> setOperations;

    @Mock private TokenManager tokenManager;

    @InjectMocks private RefreshTokenService service;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails =
                new User("user@test.com", "hash", List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void issuePersistsTokenFamilyAndUserIndex() {
        when(tokenManager.generateRefreshToken(eq(userDetails), any(), any()))
                .thenReturn("signed-token");
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        String token = service.issue(userDetails, null);

        assertEquals("signed-token", token);

        ArgumentCaptor<String> tokenKey = ArgumentCaptor.forClass(String.class);
        verify(hashOperations).put(tokenKey.capture(), eq("email"), eq("user@test.com"));
        verify(hashOperations).put(eq(tokenKey.getValue()), eq("fam"), any());
        verify(redisTemplate).expire(eq(tokenKey.getValue()), any(Duration.class));

        // Family set and user index are maintained
        verify(setOperations).add(org.mockito.ArgumentMatchers.startsWith("refresh_fam:"), any());
        verify(setOperations).add(eq("refresh_user:user@test.com"), any());
    }

    @Test
    void consumeReturnsTrueOnlyWhenTokenExisted() {
        when(redisTemplate.delete("refresh:jti-1")).thenReturn(true);
        when(redisTemplate.delete("refresh:jti-2")).thenReturn(false);

        assertTrue(service.consume("jti-1"));
        assertFalse(service.consume("jti-2"));
    }

    @Test
    void revokeFamilyDeletesAllMemberTokens() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("refresh_fam:fam-1")).thenReturn(Set.of("jti-1", "jti-2"));

        service.revokeFamily("fam-1");

        ArgumentCaptor<Collection<String>> tokenKeysCaptor =
                ArgumentCaptor.forClass(Collection.class);
        verify(redisTemplate).delete(tokenKeysCaptor.capture());
        assertEquals(
                Set.of("refresh:jti-1", "refresh:jti-2"), Set.copyOf(tokenKeysCaptor.getValue()));

        verify(redisTemplate).delete("refresh_fam:fam-1");
    }

    @Test
    void revokeAllForUserRevokesEveryFamily() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.members("refresh_user:user@test.com"))
                .thenReturn(Set.of("fam-1", "fam-2"));
        when(setOperations.members("refresh_fam:fam-1")).thenReturn(Set.of("jti-1"));
        when(setOperations.members("refresh_fam:fam-2")).thenReturn(Set.of("jti-2"));

        service.revokeAllForUser("user@test.com");

        verify(redisTemplate).delete(List.of("refresh:jti-1"));
        verify(redisTemplate).delete(List.of("refresh:jti-2"));
        verify(redisTemplate).delete("refresh_fam:fam-1");
        verify(redisTemplate).delete("refresh_fam:fam-2");
        verify(redisTemplate).delete("refresh_user:user@test.com");
    }
}
