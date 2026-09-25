package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.UserDTO;

import java.util.List;

public interface UserUseCase {

    UserDTO register(String name, String email, String rawPassword);

    List<UserDTO> getDeactivatedUsers();

    void activateUsers(List<Integer> userId);
}
