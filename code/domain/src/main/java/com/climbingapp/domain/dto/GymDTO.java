package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymDTO {

    @EqualsAndHashCode.Exclude private Integer id;

    private String name;

    private String location;

    private String description;

    @EqualsAndHashCode.Exclude private Integer creatorId;

    @EqualsAndHashCode.Exclude private LocalDateTime createdDate;

    @EqualsAndHashCode.Exclude private LocalDateTime updatedDate;
}
