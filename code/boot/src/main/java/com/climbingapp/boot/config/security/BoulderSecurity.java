package com.climbingapp.boot.config.security;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("boulderSecurity")
public class BoulderSecurity {

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private UserRepository userRepository;

    public boolean isCreator(Integer boulderId, Authentication authentication) {
        String email = authentication.getName();
        UserDTO user = userRepository.findByEmail(email);
        Integer creatorId = user != null ? user.getId() : 0;
        return boulderRepository.existsByIdAndCreatorId(boulderId, creatorId);
    }
}
