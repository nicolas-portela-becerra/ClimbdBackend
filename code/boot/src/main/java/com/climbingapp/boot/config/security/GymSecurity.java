package com.climbingapp.boot.config.security;

import com.climbingapp.infrastructure.repository.GymOwnerJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("gymSecurity")
public class GymSecurity {

    @Autowired private GymOwnerJPARepository gymOwnerRepository;

    public boolean isOwner(Integer gymId, Authentication authentication) {
        return gymOwnerRepository.existsByGymIdAndUserEmail(gymId, authentication.getName());
    }
}
