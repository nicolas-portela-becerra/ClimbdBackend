package com.climbingapp.boot.config;

import com.climbingapp.domain.dto.CompetitionBoulderDTO;
import com.climbingapp.domain.usecase.BoulderUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;

@Slf4j
@Configuration
@EnableCaching
public class CacheConfig implements CachingConfigurer {

    /** Stores competition boulders as plain JSON instead of JDK serialization. */
    @Bean
    public RedisCacheManagerBuilderCustomizer competitionBouldersCacheCustomizer(
            JsonMapper jsonMapper) {
        JacksonJsonRedisSerializer<List<CompetitionBoulderDTO>> serializer =
                new JacksonJsonRedisSerializer<>(
                        jsonMapper,
                        jsonMapper
                                .getTypeFactory()
                                .constructCollectionType(List.class, CompetitionBoulderDTO.class));
        return builder ->
                builder.getCacheConfigurationFor(BoulderUseCase.COMPETITION_BOULDERS_CACHE)
                        .ifPresent(
                                config ->
                                        builder.withCacheConfiguration(
                                                BoulderUseCase.COMPETITION_BOULDERS_CACHE,
                                                config.serializeValuesWith(
                                                        SerializationPair.fromSerializer(
                                                                serializer))));
    }

    /** If Redis is unavailable, log it and let the request go to the database. */
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache get failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCachePutError(
                    RuntimeException e, Cache cache, Object key, Object value) {
                log.warn("Cache put failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheEvictError(RuntimeException e, Cache cache, Object key) {
                log.warn("Cache evict failed on {}::{}: {}", cache.getName(), key, e.getMessage());
            }

            @Override
            public void handleCacheClearError(RuntimeException e, Cache cache) {
                log.warn("Cache clear failed on {}: {}", cache.getName(), e.getMessage());
            }
        };
    }
}
