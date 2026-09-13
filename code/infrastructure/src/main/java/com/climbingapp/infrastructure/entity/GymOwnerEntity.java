package com.climbingapp.infrastructure.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "gym_owner",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "gym_id"}))
@Getter
@Setter
@NoArgsConstructor
public class GymOwnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "gym_id", nullable = false)
    private Integer gymId;

    @Column(name = "assigned_by", nullable = false)
    private Integer assignedBy;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;
}
