package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.GymEntity;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GymJPARepository extends JpaRepository<GymEntity, Integer> {}
