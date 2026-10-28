package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.BoulderDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BoulderRepository {

    BoulderDTO findById(Integer id);

    Page<BoulderDTO> findByGymId(Integer gymId, Pageable pageable);

    Page<BoulderDTO> findByWallImageId(Integer wallImageId, Pageable pageable);

    List<BoulderDTO> findByWallImageId(Integer wallImageId);

    BoulderDTO save(BoulderDTO boulder);

    void deleteById(Integer id);

    boolean existsByIdAndCreatorId(Integer id, Integer creatorId);
}
