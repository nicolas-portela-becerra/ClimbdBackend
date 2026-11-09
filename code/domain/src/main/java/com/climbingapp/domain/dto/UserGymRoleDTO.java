package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.GymRole;

/** A gym where the user is staff, and their role there. */
public record UserGymRoleDTO(Integer gymId, String gymName, GymRole role) {}
