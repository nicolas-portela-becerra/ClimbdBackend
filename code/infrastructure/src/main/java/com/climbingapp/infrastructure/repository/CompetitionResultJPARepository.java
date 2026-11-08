package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionResultEntity;
import com.climbingapp.infrastructure.view.CompetitionRankingView;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

public interface CompetitionResultJPARepository
        extends JpaRepository<CompetitionResultEntity, Integer> {

    List<CompetitionResultEntity> findByCompetitionId(Integer competitionId);

    List<CompetitionResultEntity> findByParticipantId(Integer participantId);

    Optional<CompetitionResultEntity> findByParticipantIdAndBoulderId(
            Integer participantId, Integer boulderId);

    /**
     * Inserts or overwrites the participant's result for every given boulder in one statement,
     * calculating the points with the competition's scoring rules. Boulders that don't belong to
     * the competition are skipped.
     */
    @Modifying(clearAutomatically = true)
    @Query(
            value =
                    """
                    INSERT INTO {h-schema}competition_result
                        (participant_id, boulder_id, competition_id, attempts, topped, points)
                    SELECT :participantId, b.id, b.competition_id, x.attempts, x.topped,
                           CASE
                               WHEN NOT x.topped THEN 0
                               WHEN c.type = 'ALL_OR_NOTHING' THEN COALESCE(b.points, 0)
                               ELSE GREATEST(c.base_points - (x.attempts - 1) * c.attempt_penalty,
                                             c.min_points)
                           END
                    FROM unnest(CAST(:boulderIds AS integer[]),
                                CAST(:attempts AS integer[]),
                                CAST(:topped AS boolean[])) AS x(boulder_id, attempts, topped)
                    JOIN {h-schema}boulder b
                        ON b.id = x.boulder_id AND b.competition_id = :competitionId
                    JOIN {h-schema}competition c ON c.id = b.competition_id
                    ON CONFLICT (participant_id, boulder_id) DO UPDATE
                        SET attempts = EXCLUDED.attempts,
                            topped = EXCLUDED.topped,
                            points = EXCLUDED.points
                    """,
            nativeQuery = true)
    int upsertResults(
            @Param("participantId") Integer participantId,
            @Param("competitionId") Integer competitionId,
            @Param("boulderIds") Integer[] boulderIds,
            @Param("attempts") Integer[] attempts,
            @Param("topped") Boolean[] topped);

    @NativeQuery(
            value =
                    """
        SELECT
            u."name",
            cp.section,
            cp.gender,
            SUM(cr.points) AS total_points,
            SUM(cr.attempts) AS total_attempts,
            COUNT(*) FILTER (WHERE cr.topped AND cr.attempts = 1) AS flash_tops
        FROM {h-schema}competition c
        INNER JOIN {h-schema}competition_participant cp
            ON cp.competition_id = c.id
        INNER JOIN {h-schema}competition_result cr
            ON cr.competition_id = c.id
            AND cr.participant_id = cp.id
        INNER JOIN {h-schema}"user" u
            ON u.id = cp.user_id
        WHERE c.id = :competitionId
            AND c.date_to + make_interval(mins => c.grace_minutes) < :now
        GROUP BY u.id, u."name", cp.section, cp.gender
        ORDER BY total_points DESC, flash_tops DESC, total_attempts ASC
    """)
    List<CompetitionRankingView> getFinalCompetitionResults(
            @Param("competitionId") Integer competitionId, @Param("now") Timestamp now);
}
