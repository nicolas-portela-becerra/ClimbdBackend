package com.climbingapp.infrastructure.entity;

import com.climbingapp.infrastructure.entity.enums.GymRole;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "gym_staff",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "gym_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GymStaffEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "gym_id", nullable = false)
    private Integer gymId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private GymRole role;

    @Column(name = "assigned_by", nullable = false)
    private Integer assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private Instant assignedAt;
}
