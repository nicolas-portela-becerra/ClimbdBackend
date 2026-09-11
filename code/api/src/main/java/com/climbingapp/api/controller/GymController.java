package com.climbingapp.api.controller;

import com.climbingapp.api.dto.AssignOwnerRequest;
import com.climbingapp.api.dto.CreateGymRequest;
import com.climbingapp.api.dto.GymDto;
import com.climbingapp.api.dto.GymPageResponse;
import com.climbingapp.api.dto.UpdateGymRequest;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.GymUseCase;

import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class GymController implements GymsApi {

    @Autowired private GymUseCase gymUseCase;

    @Autowired private UserRepository userRepository;

    @Autowired private ApiDtoMapper mapper;

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<GymPageResponse> listGyms(Integer page, Integer size) {
        Page<GymDTO> gyms = gymUseCase.getAllGyms(PageRequest.of(page, size));
        return ResponseEntity.ok(mapper.toGymPage(gyms));
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<GymDto> createGym(CreateGymRequest createGymRequest) {
        GymDTO gym = buildGymDto(createGymRequest);
        GymDTO saved = gymUseCase.createGym(gym);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toApiGym(saved));
    }

    @Override
    @PreAuthorize("hasRole('USER') || hasRole('ADMIN')")
    public ResponseEntity<GymDto> getGymById(@PathVariable("gymId") Integer gymId) {
        GymDTO gym = gymUseCase.getGymById(gymId);
        if (gym == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toApiGym(gym));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<GymDto> updateGym(
            @PathVariable("gymId") Integer gymId, UpdateGymRequest updateGymRequest) {
        GymDTO gym = buildGymDto(gymId, updateGymRequest);
        GymDTO savedGym = gymUseCase.updateGym(gym);

        if (savedGym == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mapper.toApiGym(savedGym));
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignGymOwner(
            @PathVariable("gymId") Integer gymId, AssignOwnerRequest assignOwnerRequest) {
        gymUseCase.assignGymOwner(
                gymId, assignOwnerRequest.getUserId(), assignOwnerRequest.getAssignedBy());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private GymDTO buildGymDto(CreateGymRequest createGymRequest) {
        GymDTO gym = new GymDTO();
        gym.setName(createGymRequest.getName());
        gym.setLocation(createGymRequest.getLocation());
        gym.setDescription(createGymRequest.getDescription());
        gym.setCreatedByAdminId(createGymRequest.getCreatedBy());
        gym.setCreatedDate(LocalDateTime.now());
        return gym;
    }

    private GymDTO buildGymDto(Integer gymId, UpdateGymRequest updateGymRequest) {
        GymDTO gym = new GymDTO();
        gym.setId(gymId);
        gym.setDescription(updateGymRequest.getDescription());
        gym.setLocation(updateGymRequest.getLocation());
        gym.setName(updateGymRequest.getName());
        return gym;
    }
}
