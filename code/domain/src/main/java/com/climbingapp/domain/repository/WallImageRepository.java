package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.WallImageDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WallImageRepository {

    WallImageDTO findById(Integer id);

    Page<WallImageDTO> findByGymId(Integer gymId, Pageable pageable);

    List<WallImageDTO> findByGymIdAndIsActualTrue(Integer gymId);

    WallImageDTO findTopByGymIdAndWallNameAndIsActualTrueOrderByUploadedDateDesc(
            Integer gymId, String wallName);

    WallImageDTO save(WallImageDTO wallImage);
}
