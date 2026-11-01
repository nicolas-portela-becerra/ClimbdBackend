package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.CompetitionDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CompetitionRepository {

    CompetitionDTO findById(Integer id);

    Page<CompetitionDTO> findByGymId(Integer gymId, Pageable pageable);

    CompetitionDTO save(CompetitionDTO competition);

    void deleteById(Integer id);

    boolean existsByIdAndGymId(Integer id, Integer gymId);
}
