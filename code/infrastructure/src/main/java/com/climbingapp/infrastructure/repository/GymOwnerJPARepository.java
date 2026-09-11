package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.GymOwnerEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GymOwnerJPARepository extends JpaRepository<GymOwnerEntity, Integer> {

    boolean existsByGymIdAndUserId(Integer gymId, Integer userId);

    boolean existsByGymIdAndUserEmail(Integer gymId, String email);

    List<GymOwnerEntity> findByGymId(Integer gymId);
}
