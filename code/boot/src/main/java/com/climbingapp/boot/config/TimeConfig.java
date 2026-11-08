package com.climbingapp.boot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Single source of "now" for the whole application. Moments are always handled as {@link
 * java.time.Instant}, so the clock zone is irrelevant; UTC is used to keep it explicit. Tests
 * replace this bean with {@link Clock#fixed}.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
