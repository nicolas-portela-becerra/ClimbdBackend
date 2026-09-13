package com.climbingapp.boot.config.security;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.GymOwnerRepository;
import com.climbingapp.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("gymSecurity")
public class GymSecurity {

    @Autowired private GymOwnerRepository gymOwnerRepository;

    @Autowired private UserRepository userRepository;

    public boolean isOwner(Integer gymId, Authentication authentication) {
        String email = authentication.getName();
        UserDTO user = userRepository.findByEmail(email);
        Integer userId = user != null ? user.getId() : null;
        return gymOwnerRepository.existsByGymIdAndUserId(gymId, userId);
    }
}
