package com.climbingapp.boot.config.security;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.infrastructure.entity.enums.GymRole;
import com.climbingapp.jwt.utils.AuthenticatedUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * Answers every per-gym authorization question used in {@code @PreAuthorize}. Every check lets
 * admins through, so annotations don't need to repeat {@code hasRole('ADMIN')}.
 */
@Component("gymSecurity")
public class GymSecurity {

    @Autowired private GymStaffRepository gymStaffRepository;

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private CompetitionRepository competitionRepository;

    public boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                        .anyMatch(a -> AuthenticatedUser.ROLE_ADMIN.equals(a.getAuthority()));
    }

    /** Admin or OWNER of the gym: gym settings and staff management. */
    public boolean isOwner(Integer gymId, Authentication authentication) {
        return isAdmin(authentication) || roleIn(gymId, authentication) == GymRole.OWNER;
    }

    /** Admin, OWNER or SETTER of the gym: walls, competitions and every boulder of the gym. */
    public boolean isStaff(Integer gymId, Authentication authentication) {
        return isAdmin(authentication) || roleIn(gymId, authentication) != null;
    }

    public boolean isCompetitionStaff(Integer competitionId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        CompetitionDTO competition =
                competitionId != null ? competitionRepository.findById(competitionId) : null;
        return competition != null && isStaff(competition.getGymId(), authentication);
    }

    /** The creator, or staff of the boulder's gym so they can moderate user boulders. */
    public boolean canEditBoulder(Integer boulderId, Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        BoulderDTO boulder = boulderId != null ? boulderRepository.findById(boulderId) : null;
        if (boulder == null) {
            return false;
        }
        Integer userId = userId(authentication);
        return (userId != null && Objects.equals(boulder.getCreatorId(), userId))
                || isStaff(boulder.getGymId(), authentication);
    }

    private GymRole roleIn(Integer gymId, Authentication authentication) {
        return gymStaffRepository.findRole(gymId, userId(authentication));
    }

    private static Integer userId(Authentication authentication) {
        return authentication != null
                        && authentication.getPrincipal() instanceof AuthenticatedUser user
                ? user.getId()
                : null;
    }
}
