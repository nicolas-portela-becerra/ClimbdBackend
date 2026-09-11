package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.BoulderDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoulderRepository {

    BoulderDTO findById(Integer id);

    Page<BoulderDTO> findByGymId(Integer gymId, Pageable pageable);

    Page<BoulderDTO> findByWallImageId(Integer wallImageId, Pageable pageable);

    BoulderDTO save(BoulderDTO boulder);

    void deleteById(Integer id);

    boolean existsByIdAndCreatorEmail(Integer id, String email);
}
