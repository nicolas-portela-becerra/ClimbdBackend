package com.climbingapp.api.controller;

import com.climbingapp.api.dto.ActivateUsers200Response;
import com.climbingapp.api.dto.DeactivatedUsersResponse;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.usecase.UserUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
public class UserController implements UsersApi {

    @Autowired private UserUseCase userUseCase;

    @Autowired private ApiDtoMapper mapper;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ActivateUsers200Response> activateUsers(List<Integer> requestBody) {
        try {
            userUseCase.activateUsers(requestBody);
            ActivateUsers200Response response = new ActivateUsers200Response();
            response.setMessage("Users activated");
            log.info("Activated users: {}", requestBody);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to activate users: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DeactivatedUsersResponse> getDeactivated() {
        try {
            DeactivatedUsersResponse response = new DeactivatedUsersResponse();
            response.setUsers(mapper.toUserDtos(userUseCase.getDeactivatedUsers()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            log.error("Failed to fetch deactivated users: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
