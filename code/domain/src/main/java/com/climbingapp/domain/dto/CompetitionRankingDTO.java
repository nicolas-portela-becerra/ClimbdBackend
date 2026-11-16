package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.CompetitionSection;
import com.climbingapp.infrastructure.entity.enums.Gender;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionRankingDTO {

    private Integer id;

    private Integer competitionId;

    private Integer gymId;

    private String competitionName;

    private Instant dateTo;

    private Integer userId;

    private String userName;

    private CompetitionSection section;

    private Gender gender;

    private Integer totalPoints;

    private Integer totalAttempts;

    private Integer flashTops;
}
