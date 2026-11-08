package com.climbingapp.jwt.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private static final String TOKEN_KEY_PREFIX = "refresh:";
    private static final String FAMILY_KEY_PREFIX = "refresh_fam:";
    private static final String USER_KEY_PREFIX = "refresh_user:";
    private static final Duration TOKEN_TTL =
            Duration.ofSeconds(TokenManager.REFRESH_TOKEN_VALIDITY);

    @Autowired private StringRedisTemplate redisTemplate;

    @Autowired private TokenManager tokenManager;

    public String issue(UserDetails userDetails, String familyId) {
        String jti = UUID.randomUUID().toString();
        String fam = familyId != null ? familyId : UUID.randomUUID().toString();
        String token = tokenManager.generateRefreshToken(userDetails, jti, fam);

        String tokenKey = TOKEN_KEY_PREFIX + jti;
        String familyKey = FAMILY_KEY_PREFIX + fam;
        String userKey = USER_KEY_PREFIX + userDetails.getUsername();

        // Pipelined: the whole transaction is sent in a single round trip.
        redisTemplate.executePipelined(
                new SessionCallback<Object>() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Object execute(RedisOperations<K, V> operations) {
                        RedisOperations<String, String> ops =
                                (RedisOperations<String, String>) operations;
                        ops.multi();
                        ops.opsForHash()
                                .putAll(
                                        tokenKey,
                                        Map.of("email", userDetails.getUsername(), "fam", fam));
                        ops.expire(tokenKey, TOKEN_TTL);
                        ops.opsForSet().add(familyKey, jti);
                        ops.expire(familyKey, TOKEN_TTL);
                        ops.opsForSet().add(userKey, fam);
                        ops.expire(userKey, TOKEN_TTL);
                        ops.exec();
                        return null;
                    }
                });

        return token;
    }

    public boolean consume(String jti) {
        return Boolean.TRUE.equals(redisTemplate.delete(TOKEN_KEY_PREFIX + jti));
    }

    public void revokeFamily(String familyId) {
        String familyKey = FAMILY_KEY_PREFIX + familyId;
        Set<String> jtis = redisTemplate.opsForSet().members(familyKey);
        if (jtis != null && !jtis.isEmpty()) {
            List<String> tokenKeys = jtis.stream().map(jti -> TOKEN_KEY_PREFIX + jti).toList();
            redisTemplate.delete(tokenKeys);
        }
        redisTemplate.delete(familyKey);
    }

    public void revokeAllForUser(String email) {
        String userKey = USER_KEY_PREFIX + email;
        Set<String> familyIds = redisTemplate.opsForSet().members(userKey);
        if (familyIds != null) {
            familyIds.forEach(this::revokeFamily);
        }
        redisTemplate.delete(userKey);
    }
}
