package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.BoulderDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface BoulderRepository {

    BoulderDTO findById(Integer id);

    Page<BoulderDTO> findByGymId(Integer gymId, Pageable pageable);

    Page<BoulderDTO> findByWallImageId(Integer wallImageId, Pageable pageable);

    List<BoulderDTO> findByWallImageId(Integer wallImageId);

    List<BoulderDTO> findByCompetitionId(Integer competitionId);

    Page<BoulderDTO> findByWallImageIdWithoutCompetition(Integer wallImageId, Pageable pageable);

    Page<BoulderDTO> findByGymIdWithoutCompetition(Integer gymId, Pageable pageable);

    BoulderDTO save(BoulderDTO boulder);

    void deleteById(Integer id);

    boolean existsByIdAndCreatorId(Integer id, Integer creatorId);

    boolean existsByWallImageId(Integer wallImageId);

    /**
     * Turns the competition's boulders into regular ones (no competition, end date appended to the
     * name). The competition's results must be deleted first. Returns how many were updated.
     */
    int retireCompetitionBoulders(Collection<Integer> competitionIds, Instant now);
}
