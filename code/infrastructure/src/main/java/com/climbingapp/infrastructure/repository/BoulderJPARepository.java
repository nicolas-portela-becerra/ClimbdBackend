package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.BoulderEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoulderJPARepository extends JpaRepository<BoulderEntity, Integer> {

    Page<BoulderEntity> findByGymId(Integer gymId, Pageable pageable);

    Page<BoulderEntity> findByWallImageId(Integer wallImageId, Pageable pageable);

    List<BoulderEntity> findByWallImageId(Integer wallImageId);

    boolean existsByIdAndCreatorId(Integer id, Integer creatorId);
}
