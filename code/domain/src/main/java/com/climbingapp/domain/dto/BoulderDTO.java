package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoulderDTO {

    @EqualsAndHashCode.Exclude private Integer id;

    @EqualsAndHashCode.Exclude private Integer gymId;

    @EqualsAndHashCode.Exclude private Integer wallImageId;

    @EqualsAndHashCode.Exclude private Integer creatorId;

    private String name;

    private String grade;

    private String description;

    @EqualsAndHashCode.Exclude private LocalDateTime createdDate;

    @EqualsAndHashCode.Exclude private LocalDateTime updatedDate;
}
