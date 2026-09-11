package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.GymOwnerDTO;

import java.util.List;

public interface GymOwnerRepository {

    boolean existsByGymIdAndUserId(Integer gymId, Integer userId);

    List<GymOwnerDTO> findByGymId(Integer gymId);

    GymOwnerDTO save(GymOwnerDTO gymOwner);
}
