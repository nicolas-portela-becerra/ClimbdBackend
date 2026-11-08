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

@Entity
@Table(
        name = "competition_participant",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"competition_id", "user_id"}),
            @UniqueConstraint(columnNames = {"id", "competition_id"})
        })
@Getter
@Setter
@NoArgsConstructor
public class CompetitionParticipantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "section", nullable = false, columnDefinition = "comp_section")
    private CompetitionSection section;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "gender", nullable = false, columnDefinition = "gender")
    private Gender gender;

    @Column(name = "last_snapshot", nullable = false)
    private Integer lastSnapshot;

    @Column(name = "registered_at", nullable = false)
    private Instant registeredAt;
}
