package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.HoldDTO;

import java.util.List;

public interface HoldRepository {

    List<HoldDTO> findByBoulderIdOrderBySequenceOrder(Integer boulderId);

    List<HoldDTO> saveAll(List<HoldDTO> holds);

    void deleteByBoulderId(Integer boulderId);
}
