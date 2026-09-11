package com.climbingapp.boot.config.security;

import com.climbingapp.infrastructure.repository.BoulderJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("boulderSecurity")
public class BoulderSecurity {

    @Autowired private BoulderJPARepository boulderRepository;

    public boolean isCreator(Integer boulderId, Authentication authentication) {
        String email = authentication.getName();
        return boulderRepository.existsByIdAndCreatorEmail(boulderId, email);
    }
}
