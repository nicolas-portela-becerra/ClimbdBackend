package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.WallImageEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WallImageJPARepository extends JpaRepository<WallImageEntity, Integer> {

    Page<WallImageEntity> findByGymId(Integer gymId, Pageable pageable);

    Optional<WallImageEntity> findTopByGymIdAndWallNameOrderByUploadedDateDesc(
            Integer gymId, String wallName);
}
