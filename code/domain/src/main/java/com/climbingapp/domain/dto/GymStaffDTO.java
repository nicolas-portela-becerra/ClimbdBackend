package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.GymRole;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymStaffDTO {

    private Integer id;

    private Integer userId;

    private Integer gymId;

    private GymRole role;

    private Integer assignedBy;

    private Instant assignedAt;
}
