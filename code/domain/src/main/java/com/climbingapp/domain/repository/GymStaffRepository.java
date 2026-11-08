package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.GymStaffDTO;
import com.climbingapp.infrastructure.entity.enums.GymRole;

import java.util.List;

public interface GymStaffRepository {

    /** The user's role in the gym, or null when they are not staff there. */
    GymRole findRole(Integer gymId, Integer userId);

    GymStaffDTO findByGymIdAndUserId(Integer gymId, Integer userId);

    List<GymStaffDTO> findByGymId(Integer gymId);

    List<GymStaffDTO> findByUserId(Integer userId);

    GymStaffDTO save(GymStaffDTO gymStaff);

    void deleteById(Integer id);
}
