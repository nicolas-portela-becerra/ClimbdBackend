package com.climbingapp.boot.config.security;

import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.repository.CompetitionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("competitionSecurity")
public class CompetitionSecurity {

    @Autowired private CompetitionRepository competitionRepository;

    @Autowired private GymSecurity gymSecurity;

    public boolean isOwner(Integer competitionId, Authentication authentication) {
        CompetitionDTO competition =
                competitionId != null ? competitionRepository.findById(competitionId) : null;
        return competition != null && gymSecurity.isOwner(competition.getGymId(), authentication);
    }
}
