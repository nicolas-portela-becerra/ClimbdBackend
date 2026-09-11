package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.UserDTO;

public interface UserUseCase {

    UserDTO register(String name, String email, String rawPassword);
}
