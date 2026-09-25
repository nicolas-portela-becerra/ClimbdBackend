package com.climbingapp.domain.repository;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;

import java.util.List;

public interface UserRepository {

    UserDTO findByEmail(String email);

    UserCredentials findCredentialsByEmail(String email);

    UserDTO save(UserDTO user);

    UserDTO saveWithPassword(UserDTO user, String passwordHash);

    UserDTO findById(int userId);

    void activateUsers(List<Integer> userId);

    List<UserDTO> findDeactivatedUsers();
}
