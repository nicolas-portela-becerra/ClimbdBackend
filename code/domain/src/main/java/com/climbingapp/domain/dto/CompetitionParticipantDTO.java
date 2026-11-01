package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.CompetitionSection;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionParticipantDTO {

    private Integer id;

    private Integer userId;

    private Integer competitionId;

    private CompetitionSection section;

    private LocalDateTime registeredAt;
}
