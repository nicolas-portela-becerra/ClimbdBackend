package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface CompetitionRepository {

    CompetitionDTO findById(Integer id);

    Page<CompetitionDTO> findByGymId(Integer gymId, Pageable pageable);

    CompetitionDTO save(CompetitionDTO competition);

    void deleteById(Integer id);

    boolean existsByIdAndGymId(Integer id, Integer gymId);

    /** Competitions of the gym that have not ended yet (upcoming or running). */
    List<CompetitionDTO> findNotEndedByGymId(Integer gymId, Instant endedAfter);
}
