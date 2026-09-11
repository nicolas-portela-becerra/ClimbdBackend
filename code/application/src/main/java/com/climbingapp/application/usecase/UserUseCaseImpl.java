package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.UserUseCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserUseCaseImpl implements UserUseCase {

    @Autowired private UserRepository userRepository;

    @Autowired private PasswordEncoder passwordEncoder;

    @Override
    public UserDTO register(String name, String email, String rawPassword) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw new DataIntegrityViolationException("Duplicate registration");
        }
        UserDTO newUser = buildUser(name, normalizedEmail);
        return userRepository.saveWithPassword(newUser, passwordEncoder.encode(rawPassword));
    }

    private UserDTO buildUser(String name, String normalizedEmail) {
        UserDTO newUser = new UserDTO();
        newUser.setEmail(normalizedEmail);
        newUser.setName(name);
        newUser.setRole("USER");
        newUser.setProvider("LOCAL");
        newUser.setCreatedDate(LocalDateTime.now());
        return newUser;
    }
}
