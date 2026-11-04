package com.climbingapp.infrastructure.entity;

import com.climbingapp.infrastructure.entity.enums.CompetitionType;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "competition", uniqueConstraints = @UniqueConstraint(columnNames = {"id", "gym_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CompetitionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "gym_id", nullable = false)
    private Integer gymId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "type", nullable = false, columnDefinition = "comp_type")
    private CompetitionType type;

    @Column(name = "grace_minutes", nullable = false)
    private Integer graceMinutes;

    @Column(name = "base_points")
    private Integer basePoints;

    @Column(name = "attempt_penalty")
    private Integer attemptPenalty;

    @Column(name = "min_points")
    private Integer minPoints;

    @Column(name = "date_from", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDateTime dateTo;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
