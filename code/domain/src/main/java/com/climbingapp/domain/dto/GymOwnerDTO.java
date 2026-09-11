package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymOwnerDTO {

    private Integer id;

    private Integer userId;

    private Integer gymId;

    private Integer assignedBy;

    private LocalDateTime assignedAt;
}
