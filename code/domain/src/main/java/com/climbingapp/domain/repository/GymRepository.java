package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.GymDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GymRepository {

    GymDTO findById(Integer id);

    GymDTO save(GymDTO gym);

    Page<GymDTO> findAll(Pageable pageable);

    void deleteById(Integer id);
}
