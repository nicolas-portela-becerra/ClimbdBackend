package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionRankingRecord;
import com.climbingapp.domain.dto.CompetitionResultDTO;

import java.sql.Timestamp;
import java.util.List;

public interface CompetitionResultRepository {

    CompetitionResultDTO findById(Integer id);

    List<CompetitionResultDTO> findByCompetitionId(Integer competitionId);

    List<CompetitionResultDTO> findByParticipantId(Integer participantId);

    CompetitionResultDTO findByParticipantIdAndBoulderId(Integer participantId, Integer boulderId);

    /** Inserts or overwrites the given results, calculating their points. */
    void upsertResults(
            Integer participantId, Integer competitionId, List<CompetitionResultDTO> results);

    CompetitionResultDTO save(CompetitionResultDTO result);

    void deleteById(Integer id);

    List<CompetitionRankingRecord> getCompetitionRanking(Integer competitionId, Timestamp now);
}
