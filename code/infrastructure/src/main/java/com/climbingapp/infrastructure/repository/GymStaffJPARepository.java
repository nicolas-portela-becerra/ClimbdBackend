package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.GymStaffEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GymStaffJPARepository extends JpaRepository<GymStaffEntity, Integer> {

    Optional<GymStaffEntity> findByGymIdAndUserId(Integer gymId, Integer userId);

    List<GymStaffEntity> findByGymId(Integer gymId);

    List<GymStaffEntity> findByUserId(Integer userId);
}
