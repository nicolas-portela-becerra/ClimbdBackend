package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionParticipantEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompetitionParticipantJPARepository
        extends JpaRepository<CompetitionParticipantEntity, Integer> {

    List<CompetitionParticipantEntity> findByCompetitionId(Integer competitionId);

    Optional<CompetitionParticipantEntity> findByCompetitionIdAndUserId(
            Integer competitionId, Integer userId);

    boolean existsByCompetitionIdAndUserId(Integer competitionId, Integer userId);

    /** Stores the snapshot only if it's newer. Returns 0 when it was older or equal. */
    @Modifying(clearAutomatically = true)
    @Query(
            "update CompetitionParticipantEntity p set p.lastSnapshot = :snapshot"
                    + " where p.id = :id and p.lastSnapshot < :snapshot")
    int advanceSnapshot(@Param("id") Integer id, @Param("snapshot") Integer snapshot);
}
