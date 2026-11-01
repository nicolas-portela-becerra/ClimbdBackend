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
    @Column(name = "type", columnDefinition = "comp_type")
    private CompetitionType type;

    @Column(name = "max_points", nullable = false)
    private Integer maxPoints;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "date_from", nullable = false)
    private LocalDateTime dateFrom;

    @Column(name = "date_to", nullable = false)
    private LocalDateTime dateTo;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
}
