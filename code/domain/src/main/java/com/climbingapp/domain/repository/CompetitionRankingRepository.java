package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionRankingDTO;

import java.util.Collection;
import java.util.List;

public interface CompetitionRankingRepository {

    /** Stored final ranking of the competition, best first. Empty if it wasn't stored yet. */
    List<CompetitionRankingDTO> findByCompetitionId(Integer competitionId);

    /**
     * Stores the ranking of the given competitions, calculated from its results. Must run before
     * the results are deleted. Returns the number of rows inserted (0 if it was already stored).
     */
    int saveFinalRanking(Collection<Integer> competitionIds);
}
