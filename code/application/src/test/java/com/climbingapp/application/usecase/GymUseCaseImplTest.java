package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.GymStaffDTO;
import com.climbingapp.domain.dto.GymStaffMemberDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.exception.ForbiddenException;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.climbingapp.infrastructure.entity.enums.GymRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class GymUseCaseImplTest {

    private static final Instant NOW = Instant.parse("2026-11-20T18:00:00Z");
    private static final int GYM = 10;
    private static final int OWNER_ID = 2;

    private final GymRepository gymRepository = mock(GymRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final GymStaffRepository staffRepository = mock(GymStaffRepository.class);
    private final ImageProcessorUseCase imageProcessor = mock(ImageProcessorUseCase.class);

    private final GymUseCaseImpl useCase = new GymUseCaseImpl();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "gymRepository", gymRepository);
        ReflectionTestUtils.setField(useCase, "userRepository", userRepository);
        ReflectionTestUtils.setField(useCase, "gymStaffRepository", staffRepository);
        ReflectionTestUtils.setField(useCase, "imageProcessorUseCase", imageProcessor);
        ReflectionTestUtils.setField(useCase, "clock", Clock.fixed(NOW, ZoneOffset.UTC));
        when(gymRepository.findById(GYM)).thenReturn(gym());
        when(staffRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByEmail("setter@test.com")).thenReturn(user(3));
        when(userRepository.findByEmail("owner@test.com")).thenReturn(user(OWNER_ID));
        when(staffRepository.findByGymIdAndUserId(GYM, OWNER_ID))
                .thenReturn(staff(OWNER_ID, GymRole.OWNER));
    }

    @Test
    void ownerAddsASetterByEmail() {
        GymStaffMemberDTO member =
                useCase.addGymStaff(GYM, " Setter@Test.com ", GymRole.SETTER, OWNER_ID, false);

        assertEquals(3, member.userId());
        assertEquals(GymRole.SETTER, member.role());
        assertEquals(NOW, member.assignedAt());
    }

    @Test
    void ownerCannotGrantTheOwnerRole() {
        assertThrows(
                ForbiddenException.class,
                () -> useCase.addGymStaff(GYM, "setter@test.com", GymRole.OWNER, OWNER_ID, false));
        verify(staffRepository, never()).save(any());
    }

    @Test
    void ownerCannotDemoteThemselves() {
        assertThrows(
                ForbiddenException.class,
                () -> useCase.addGymStaff(GYM, "owner@test.com", GymRole.SETTER, OWNER_ID, false));
    }

    @Test
    void ownerCannotRemoveAnOwnerButAdminCan() {
        assertThrows(ForbiddenException.class, () -> useCase.removeGymStaff(GYM, OWNER_ID, false));
        verify(staffRepository, never()).deleteById(any());

        useCase.removeGymStaff(GYM, OWNER_ID, true);
        verify(staffRepository).deleteById(77);
    }

    @Test
    void adminCanGrantTheOwnerRole() {
        GymStaffMemberDTO member =
                useCase.addGymStaff(GYM, "setter@test.com", GymRole.OWNER, 1, true);
        assertEquals(GymRole.OWNER, member.role());
    }

    @Test
    void unknownEmailIsNotFound() {
        assertThrows(
                NotFoundException.class,
                () -> useCase.addGymStaff(GYM, "nobody@test.com", GymRole.SETTER, OWNER_ID, false));
    }

    @Test
    void updateGymKeepsCreatorAndLogoWhenNotSent() {
        when(gymRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        GymDTO request = new GymDTO();
        request.setId(GYM);
        request.setDescription("New description");

        GymDTO saved = useCase.updateGym(request);

        assertEquals(7, saved.getCreatorId());
        assertArrayEquals(new byte[] {1}, saved.getLogo());
        assertEquals("Gym", saved.getName());
        assertEquals("Europe/Madrid", saved.getTimeZone());
        assertEquals("New description", saved.getDescription());
        assertEquals(NOW, saved.getUpdatedDate());
    }

    @Test
    void updateGymResizesANewLogo() {
        when(gymRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(imageProcessor.resize(new byte[] {9}, 512, 512)).thenReturn(new byte[] {5});
        GymDTO request = new GymDTO();
        request.setId(GYM);
        request.setLogo(new byte[] {9});

        assertArrayEquals(new byte[] {5}, useCase.updateGym(request).getLogo());
    }

    private static GymDTO gym() {
        GymDTO gym = new GymDTO();
        gym.setId(GYM);
        gym.setName("Gym");
        gym.setCreatorId(7);
        gym.setLogo(new byte[] {1});
        gym.setTimeZone("Europe/Madrid");
        gym.setCreatedDate(NOW.minusSeconds(3600));
        return gym;
    }

    private static UserDTO user(int id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setEmail("u" + id + "@test.com");
        user.setName("User " + id);
        return user;
    }

    private static GymStaffDTO staff(int userId, GymRole role) {
        return new GymStaffDTO(77, userId, GYM, role, 1, NOW);
    }
}
