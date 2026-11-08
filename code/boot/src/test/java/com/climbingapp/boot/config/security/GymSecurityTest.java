package com.climbingapp.boot.config.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.infrastructure.entity.enums.GymRole;
import com.climbingapp.jwt.utils.AuthenticatedUser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

class GymSecurityTest {

    private static final int GYM = 10;
    private static final int OTHER_GYM = 20;
    private static final int COMPETITION = 50;
    private static final int USER_BOULDER = 100;

    private final Authentication admin = auth(1, "ADMIN");
    private final Authentication owner = auth(2, "USER");
    private final Authentication setter = auth(3, "USER");
    private final Authentication otherGymSetter = auth(4, "USER");
    private final Authentication climber = auth(5, "USER");
    private final Authentication anotherClimber = auth(6, "USER");

    private final GymSecurity security = new GymSecurity();

    @BeforeEach
    void setUp() {
        GymStaffRepository staff = mock(GymStaffRepository.class);
        when(staff.findRole(any(), any())).thenReturn(null);
        when(staff.findRole(GYM, 2)).thenReturn(GymRole.OWNER);
        when(staff.findRole(GYM, 3)).thenReturn(GymRole.SETTER);
        when(staff.findRole(OTHER_GYM, 4)).thenReturn(GymRole.SETTER);

        BoulderDTO boulder = new BoulderDTO();
        boulder.setId(USER_BOULDER);
        boulder.setGymId(GYM);
        boulder.setCreatorId(5);
        BoulderRepository boulders = mock(BoulderRepository.class);
        when(boulders.findById(USER_BOULDER)).thenReturn(boulder);

        CompetitionDTO competition = new CompetitionDTO();
        competition.setId(COMPETITION);
        competition.setGymId(GYM);
        CompetitionRepository competitions = mock(CompetitionRepository.class);
        when(competitions.findById(COMPETITION)).thenReturn(competition);

        ReflectionTestUtils.setField(security, "gymStaffRepository", staff);
        ReflectionTestUtils.setField(security, "boulderRepository", boulders);
        ReflectionTestUtils.setField(security, "competitionRepository", competitions);
    }

    @Test
    void onlyAdminAndOwnerManageTheGym() {
        assertTrue(security.isOwner(GYM, admin));
        assertTrue(security.isOwner(GYM, owner));
        assertFalse(security.isOwner(GYM, setter));
        assertFalse(security.isOwner(GYM, climber));
        assertFalse(security.isOwner(OTHER_GYM, owner));
    }

    @Test
    void ownersAndSettersAreStaffOnlyInTheirGym() {
        assertTrue(security.isStaff(GYM, admin));
        assertTrue(security.isStaff(GYM, owner));
        assertTrue(security.isStaff(GYM, setter));
        assertFalse(security.isStaff(GYM, otherGymSetter));
        assertFalse(security.isStaff(GYM, climber));
    }

    @Test
    void settersRunTheCompetitionsOfTheirGym() {
        assertTrue(security.isCompetitionStaff(COMPETITION, setter));
        assertTrue(security.isCompetitionStaff(COMPETITION, owner));
        assertTrue(security.isCompetitionStaff(COMPETITION, admin));
        assertFalse(security.isCompetitionStaff(COMPETITION, otherGymSetter));
        assertFalse(security.isCompetitionStaff(COMPETITION, climber));
        assertFalse(security.isCompetitionStaff(999, setter));
    }

    @Test
    void staffCanModerateBouldersCreatedByUsers() {
        assertTrue(security.canEditBoulder(USER_BOULDER, climber)); // creator
        assertTrue(security.canEditBoulder(USER_BOULDER, setter));
        assertTrue(security.canEditBoulder(USER_BOULDER, owner));
        assertTrue(security.canEditBoulder(USER_BOULDER, admin));
        assertFalse(security.canEditBoulder(USER_BOULDER, otherGymSetter));
        assertFalse(security.canEditBoulder(USER_BOULDER, anotherClimber));
        assertFalse(security.canEditBoulder(999, setter));
    }

    private static Authentication auth(int id, String role) {
        AuthenticatedUser user =
                new AuthenticatedUser(
                        new UserCredentials(id, "u" + id + "@test.com", "hash", role, true));
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
