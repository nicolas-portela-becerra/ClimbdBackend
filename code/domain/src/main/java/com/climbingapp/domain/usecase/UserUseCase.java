package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.infrastructure.entity.enums.Gender;

import java.util.List;

public interface UserUseCase {

    UserDTO register(String name, String email, String rawPassword, Gender gender);

    List<UserDTO> getDeactivatedUsers();

    void activateUsers(List<Integer> userId);
}
