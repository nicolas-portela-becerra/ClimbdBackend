package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionParticipantDTO;

import java.util.Collection;
import java.util.List;

public interface CompetitionParticipantRepository {

    CompetitionParticipantDTO findById(Integer id);

    List<CompetitionParticipantDTO> findByCompetitionId(Integer competitionId);

    CompetitionParticipantDTO findByCompetitionIdAndUserId(Integer competitionId, Integer userId);

    boolean existsByCompetitionIdAndUserId(Integer competitionId, Integer userId);

    /** Stores the snapshot only if it's newer than the stored one. */
    boolean advanceSnapshot(Integer participantId, Integer snapshot);

    CompetitionParticipantDTO save(CompetitionParticipantDTO participant);

    void deleteById(Integer id);

    /** Deletes every participant of the given competitions. Their results must be gone first. */
    void deleteByCompetitionIds(Collection<Integer> competitionIds);
}
