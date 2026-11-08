package com.climbingapp.jwt.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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
    @SuppressWarnings("unchecked")
    void issuePersistsTokenFamilyAndUserIndexInOneTransaction() {
        when(tokenManager.generateRefreshToken(eq(userDetails), any(), any()))
                .thenReturn("signed-token");
        // Run the callback against the mocked template, as Redis would on the bound connection
        when(redisTemplate.executePipelined(any(SessionCallback.class)))
                .thenAnswer(
                        invocation -> {
                            invocation
                                    .<SessionCallback<Object>>getArgument(0)
                                    .execute(redisTemplate);
                            return List.of();
                        });
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);

        String token = service.issue(userDetails, null);

        assertEquals("signed-token", token);

        ArgumentCaptor<String> tokenKey = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Map<Object, Object>> fields = ArgumentCaptor.forClass(Map.class);
        InOrder inOrder = inOrder(redisTemplate, hashOperations, setOperations);
        inOrder.verify(redisTemplate).multi();
        inOrder.verify(hashOperations).putAll(tokenKey.capture(), fields.capture());
        inOrder.verify(redisTemplate).expire(eq(tokenKey.getValue()), any(Duration.class));
        // Family set and user index are maintained
        inOrder.verify(setOperations).add(startsWith("refresh_fam:"), any());
        inOrder.verify(setOperations).add(eq("refresh_user:user@test.com"), any());
        inOrder.verify(redisTemplate).exec();

        assertTrue(tokenKey.getValue().startsWith("refresh:"));
        assertEquals("user@test.com", fields.getValue().get("email"));
        assertTrue(fields.getValue().containsKey("fam"));
        verify(redisTemplate, times(3)).expire(any(String.class), any(Duration.class));
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
