package com.climbingapp.infrastructure.entity;

import com.climbingapp.infrastructure.entity.enums.CompetitionSection;
import com.climbingapp.infrastructure.entity.enums.Gender;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/** Final ranking of a finished competition. Has no foreign keys so it outlives its sources. */
@Entity
@Table(
        name = "competition_ranking",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"competition_id", "user_id"})})
@Getter
@Setter
@NoArgsConstructor
public class CompetitionRankingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;

    @Column(name = "gym_id", nullable = false)
    private Integer gymId;

    @Column(name = "competition_name", nullable = false)
    private String competitionName;

    @Column(name = "date_to", nullable = false)
    private Instant dateTo;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "user_name")
    private String userName;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "section", nullable = false, columnDefinition = "comp_section")
    private CompetitionSection section;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, columnDefinition = "gender")
    private Gender gender;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints;

    @Column(name = "total_attempts", nullable = false)
    private Integer totalAttempts;

    @Column(name = "flash_tops", nullable = false)
    private Integer flashTops;
}
