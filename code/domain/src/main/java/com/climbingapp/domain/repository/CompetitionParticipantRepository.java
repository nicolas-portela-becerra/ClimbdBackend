package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionParticipantDTO;

import java.util.List;

public interface CompetitionParticipantRepository {

    CompetitionParticipantDTO findById(Integer id);

    List<CompetitionParticipantDTO> findByCompetitionId(Integer competitionId);

    CompetitionParticipantDTO findByCompetitionIdAndUserId(Integer competitionId, Integer userId);

    boolean existsByCompetitionIdAndUserId(Integer competitionId, Integer userId);

    CompetitionParticipantDTO save(CompetitionParticipantDTO participant);

    void deleteById(Integer id);
}
