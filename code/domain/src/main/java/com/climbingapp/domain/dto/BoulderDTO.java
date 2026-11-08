package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoulderDTO {

    @EqualsAndHashCode.Exclude private Integer id;

    @EqualsAndHashCode.Exclude private Integer gymId;

    @EqualsAndHashCode.Exclude private Integer wallImageId;

    @EqualsAndHashCode.Exclude private Integer creatorId;

    @EqualsAndHashCode.Exclude private Integer competitionId;

    @EqualsAndHashCode.Exclude private Integer points;

    private String name;

    private String grade;

    private String description;

    @EqualsAndHashCode.Exclude private Instant createdDate;

    @EqualsAndHashCode.Exclude private Instant updatedDate;
}
