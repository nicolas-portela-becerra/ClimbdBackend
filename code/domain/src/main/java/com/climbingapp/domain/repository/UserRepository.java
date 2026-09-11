package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;

public interface UserRepository {

    UserDTO findByEmail(String email);

    UserCredentials findCredentialsByEmail(String email);

    UserDTO save(UserDTO user);

    UserDTO saveWithPassword(UserDTO user, String passwordHash);

    UserDTO findById(int userId);
}
