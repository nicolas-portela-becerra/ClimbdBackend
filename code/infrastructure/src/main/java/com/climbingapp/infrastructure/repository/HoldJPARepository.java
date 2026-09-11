package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.HoldEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HoldJPARepository extends JpaRepository<HoldEntity, Integer> {

    List<HoldEntity> findByBoulderIdOrderBySequenceOrder(Integer boulderId);

    void deleteByBoulderId(Integer boulderId);
}
