package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class UserUseCaseImplTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserUseCaseImpl userUseCase;

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

        UserDTO saved = userUseCase.register("Test", " User@Test.COM ", "password123");

        ArgumentCaptor<UserDTO> userCaptor = ArgumentCaptor.forClass(UserDTO.class);
        ArgumentCaptor<String> hashCaptor = ArgumentCaptor.forClass(String.class);
        verify(userRepository).saveWithPassword(userCaptor.capture(), hashCaptor.capture());
        UserDTO toSave = userCaptor.getValue();
        assertEquals("user@test.com", toSave.getEmail());
        assertEquals("Test", toSave.getName());
        assertEquals("USER", toSave.getRole());
        assertEquals("LOCAL", toSave.getProvider());
        assertEquals("hash", hashCaptor.getValue()); // hash now travels separately
        assertNotNull(toSave.getCreatedDate());
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
                        LocalDateTime.now(),
                        null); // 8 args — no hash
        when(userRepository.findByEmail("user@test.com")).thenReturn(existing);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> userUseCase.register("Test", "user@test.com", "password123"));

        verify(userRepository, never()).saveWithPassword(any(), anyString());
        verify(passwordEncoder, never()).encode(anyString());
    }
}
