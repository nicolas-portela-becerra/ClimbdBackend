package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionResultEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompetitionResultJPARepository
        extends JpaRepository<CompetitionResultEntity, Integer> {

    List<CompetitionResultEntity> findByCompetitionId(Integer competitionId);

    List<CompetitionResultEntity> findByParticipantId(Integer participantId);

    Optional<CompetitionResultEntity> findByParticipantIdAndBoulderId(
            Integer participantId, Integer boulderId);
}
