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
public class CompetitionParticipantDTO {

    private Integer id;

    private Integer userId;

    private Integer competitionId;

    private CompetitionSection section;

    private Gender gender;

    private Integer lastSnapshot;

    private Instant registeredAt;
}
