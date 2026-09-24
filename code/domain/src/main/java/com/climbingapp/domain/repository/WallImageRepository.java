package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.WallImageDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WallImageRepository {

    WallImageDTO findById(Integer id);

    Page<WallImageDTO> findByGymId(Integer gymId, Pageable pageable);

    WallImageDTO save(WallImageDTO wallImage);
}
