package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionResultDTO;

import java.util.List;

public interface CompetitionResultRepository {

    CompetitionResultDTO findById(Integer id);

    List<CompetitionResultDTO> findByCompetitionId(Integer competitionId);

    List<CompetitionResultDTO> findByParticipantId(Integer participantId);

    CompetitionResultDTO findByParticipantIdAndBoulderId(Integer participantId, Integer boulderId);

    CompetitionResultDTO save(CompetitionResultDTO result);

    void deleteById(Integer id);
}
