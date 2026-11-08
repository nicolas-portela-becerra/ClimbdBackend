package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.infrastructure.entity.enums.Gender;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

@ExtendWith(MockitoExtension.class)
class UserUseCaseImplTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserUseCaseImpl userUseCase;

    private static final Instant NOW = Instant.parse("2026-11-20T18:00:00Z");

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userUseCase, "clock", Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void registerNormalizesEmailEncodesPasswordAndSavesLocalUser() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(null);
        when(passwordEncoder.encode("password123")).thenReturn("hash");
        when(userRepository.saveWithPassword(any(UserDTO.class), anyString()))
                .thenAnswer(
                        invocation -> {
                            UserDTO user = invocation.getArgument(0);
                            user.setId(1);
                            return user;
                        });

        UserDTO saved = userUseCase.register("Test", " User@Test.COM ", "password123", Gender.MAN);

        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).saveWithPassword(userCaptor.capture(), hashCaptor.capture());
        UserDTO toSave = userCaptor.getValue();
        assertEquals("user@test.com", toSave.getEmail());
        assertEquals("Test", toSave.getName());
        assertEquals("USER", toSave.getRole());
        assertEquals("LOCAL", toSave.getProvider());
        assertEquals(Gender.MAN, toSave.getGender());
        assertEquals("hash", hashCaptor.getValue()); // hash now travels separately
        assertEquals(NOW, toSave.getCreatedDate());
        assertEquals(1, saved.getId());
    }

    @Test
    void registerWithExistingEmailThrowsConflictWithoutRevealingReason() {
        UserDTO existing =
                new UserDTO(
                        1,
                        "user@test.com",
                        "Test",
                        "USER",
                        "LOCAL",
                        null,
                        true,
                        NOW,
                        null,
                        null); // no hash
        when(userRepository.findByEmail("user@test.com")).thenReturn(existing);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userUseCase.register("Test", "user@test.com", "password123", Gender.MAN));

        verify(userRepository, never()).saveWithPassword(any(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void registerWithoutGenderIsRejectedBeforeSaving() {
        assertThrows(
                IllegalArgumentException.class,
                () -> userUseCase.register("Test", "user@test.com", "password123", null));
        verify(userRepository, never()).saveWithPassword(any(), any());
    }
}
