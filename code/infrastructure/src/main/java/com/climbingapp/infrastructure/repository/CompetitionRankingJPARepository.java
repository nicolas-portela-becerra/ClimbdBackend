package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionRankingEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CompetitionRankingJPARepository
        extends JpaRepository<CompetitionRankingEntity, Integer> {

    List<CompetitionRankingEntity>
            findByCompetitionIdOrderByTotalPointsDescFlashTopsDescTotalAttemptsAsc(
                    Integer competitionId);

    @Modifying(clearAutomatically = true)
    @Query(
            value =
                    """
                    INSERT INTO {h-schema}competition_ranking
                        (competition_id, gym_id, competition_name, date_to, user_id, user_name,
                         "section", gender, total_points, total_attempts, flash_tops)
                    SELECT c.id, c.gym_id, c."name", c.date_to, u.id, u."name",
                           cp.section, cp.gender,
                           SUM(cr.points),
                           SUM(cr.attempts),
                           COUNT(*) FILTER (WHERE cr.topped AND cr.attempts = 1)
                    FROM {h-schema}competition c
                    INNER JOIN {h-schema}competition_participant cp
                        ON cp.competition_id = c.id
                    INNER JOIN {h-schema}competition_result cr
                        ON cr.competition_id = c.id
                        AND cr.participant_id = cp.id
                    INNER JOIN {h-schema}"user" u
                        ON u.id = cp.user_id
                    WHERE c.id IN (:competitionIds)
                    GROUP BY c.id, u.id, u."name", cp.section, cp.gender
                    ON CONFLICT (competition_id, user_id) DO NOTHING
                    """,
            nativeQuery = true)
    int saveFinalRanking(@Param("competitionIds") Collection<Integer> competitionIds);
}
