package com.climbingapp.domain.dto;

import com.climbingapp.infrastructure.entity.enums.GymRole;

import java.time.Instant;

/** A staff member of a gym, with the user details the staff screen shows. */
public record GymStaffMemberDTO(
        Integer userId, String name, String email, GymRole role, Instant assignedAt) {}
