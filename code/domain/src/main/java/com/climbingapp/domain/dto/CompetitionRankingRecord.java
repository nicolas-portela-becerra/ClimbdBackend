package com.climbingapp.domain.dto;

public record CompetitionRankingRecord(
        String name,
        String section,
        String gender,
        Integer totalPoints,
        Integer totalAttempts,
        Integer flashTops) {}
