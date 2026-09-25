package com.climbingapp.domain.dto;

public record UserCredentials(
        Integer id, String email, String passwordHash, String role, boolean active) {}
