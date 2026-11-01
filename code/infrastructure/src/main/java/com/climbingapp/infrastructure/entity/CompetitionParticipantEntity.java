package com.climbingapp.infrastructure.entity;

import com.climbingapp.infrastructure.entity.enums.CompetitionSection;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

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
    @Column(name = "section", columnDefinition = "comp_section")
    private CompetitionSection section;

    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;
}
