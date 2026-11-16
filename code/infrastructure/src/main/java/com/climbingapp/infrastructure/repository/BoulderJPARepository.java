package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.BoulderEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BoulderJPARepository extends JpaRepository<BoulderEntity, Integer> {

    Page<BoulderEntity> findByGymId(Integer gymId, Pageable pageable);

    Page<BoulderEntity> findByWallImageId(Integer wallImageId, Pageable pageable);

    List<BoulderEntity> findByWallImageId(Integer wallImageId);

    List<BoulderEntity> findByCompetitionId(Integer competitionId);

    Page<BoulderEntity> findByWallImageIdAndCompetitionIdIsNull(
            Integer wallImageId, Pageable pageable);

    Page<BoulderEntity> findByGymIdAndCompetitionIdIsNull(Integer gymId, Pageable pageable);

    boolean existsByIdAndCreatorId(Integer id, Integer creatorId);

    boolean existsByWallImageId(Integer wallImageId);

    @Modifying(clearAutomatically = true)
    @Query(
            value =
                    """
                    UPDATE {h-schema}boulder b
                    SET competition_id = NULL,
                        points = NULL,
                        "name" = LEFT(b."name" || ' - '
                                 || to_char(c.date_to AT TIME ZONE g.time_zone, 'YYYY-MM-DD'), 255),
                        updated_date = :now
                    FROM {h-schema}competition c
                    INNER JOIN {h-schema}gym g ON g.id = c.gym_id
                    WHERE b.competition_id = c.id
                        AND c.id IN (:competitionIds)
                    """,
            nativeQuery = true)
    int retireCompetitionBoulders(
            @Param("competitionIds") Collection<Integer> competitionIds, @Param("now") Instant now);
}
