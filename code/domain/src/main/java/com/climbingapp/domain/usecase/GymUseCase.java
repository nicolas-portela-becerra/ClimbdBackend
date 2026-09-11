package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.GymDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GymUseCase {

    GymDTO createGym(GymDTO gymDTO);

    GymDTO getGymById(int id);

    Page<GymDTO> getAllGyms(Pageable pageable);

    GymDTO updateGym(GymDTO gymDTO);

    void assignGymOwner(int gymId, int userId, int assigneeId);
}
