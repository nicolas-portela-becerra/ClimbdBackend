package com.climbingapp.infrastructure.entity;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "competition_result",
        uniqueConstraints = @UniqueConstraint(columnNames = {"participant_id", "boulder_id"}))
@Getter
@Setter
@NoArgsConstructor
public class CompetitionResultEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "participant_id", nullable = false)
    private Integer participantId;

    @Column(name = "boulder_id", nullable = false)
    private Integer boulderId;

    @Column(name = "competition_id", nullable = false)
    private Integer competitionId;

    @Column(name = "attempts", nullable = false)
    private Integer attempts;

    @Column(name = "topped", nullable = false)
    private boolean topped;

    @Column(name = "points", nullable = false)
    private Integer points;
}
