package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionParticipantEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionParticipantJPARepository
        extends JpaRepository<CompetitionParticipantEntity, Integer> {

    List<CompetitionParticipantEntity> findByCompetitionId(Integer competitionId);

    Optional<CompetitionParticipantEntity> findByCompetitionIdAndUserId(
            Integer competitionId, Integer userId);

    boolean existsByCompetitionIdAndUserId(Integer competitionId, Integer userId);
}
