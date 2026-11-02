package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.CompetitionType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionDTO {

    private Integer id;

    private Integer gymId;

    private String name;

    private CompetitionType type;

    private Integer graceMinutes;

    private Integer basePoints;

    private Integer penaltyPerAttempt;

    private Integer minPoints;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private LocalDateTime createdDate;
}
