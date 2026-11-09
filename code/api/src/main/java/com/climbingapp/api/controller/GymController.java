package com.climbingapp.api.controller;

import com.climbingapp.api.dto.AddGymStaffRequest;
import com.climbingapp.api.dto.CreateGymRequest;
import com.climbingapp.api.dto.GymDto;
import com.climbingapp.api.dto.GymPageResponse;
import com.climbingapp.api.dto.GymStaffDto;
import com.climbingapp.api.dto.UpdateGymRequest;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.usecase.GymUseCase;
import com.climbingapp.infrastructure.entity.enums.GymRole;
import com.climbingapp.jwt.utils.AuthenticatedUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GymController implements GymsApi {

    @Autowired private GymUseCase gymUseCase;

    @Autowired private ApiDtoMapper mapper;

    @Override
    public ResponseEntity<GymPageResponse> listGyms(Integer page, Integer size) {
        Page<GymDTO> gyms = gymUseCase.getAllGyms(PageRequest.of(page, size));
        return ResponseEntity.ok(mapper.toGymPage(gyms));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GymDto> createGym(CreateGymRequest createGymRequest) {
        GymDTO gym = buildGymDto(createGymRequest);
        GymDTO saved = gymUseCase.createGym(gym);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toApiGym(saved));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteGym(@PathVariable("gymId") Integer gymId) {
        if (gymUseCase.getGymById(gymId) == null) {
            throw new NotFoundException("Gym with id " + gymId + " does not exist.");
        }
        gymUseCase.deleteGymById(gymId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<GymDto> getGymById(@PathVariable("gymId") Integer gymId) {
        GymDTO gym = gymUseCase.getGymById(gymId);
        if (gym == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toApiGym(gym));
    }

    @Override
    @PreAuthorize("@gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<GymDto> updateGym(
            @PathVariable("gymId") Integer gymId, UpdateGymRequest updateGymRequest) {
        GymDTO savedGym = gymUseCase.updateGym(buildGymDto(gymId, updateGymRequest));
        if (savedGym == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toApiGym(savedGym));
    }

    @Override
    @PreAuthorize("@gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<List<GymStaffDto>> listGymStaff(@PathVariable("gymId") Integer gymId) {
        return ResponseEntity.ok(mapper.toApiStaff(gymUseCase.getGymStaff(gymId)));
    }

    @Override
    @PreAuthorize("@gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<GymStaffDto> addGymStaff(
            @PathVariable("gymId") Integer gymId, AddGymStaffRequest addGymStaffRequest) {
        AuthenticatedUser actor = AuthenticatedUser.current();
        return ResponseEntity.ok(
                mapper.toApiStaff(
                        gymUseCase.addGymStaff(
                                gymId,
                                addGymStaffRequest.getEmail(),
                                GymRole.valueOf(addGymStaffRequest.getRole().name()),
                                actor.getId(),
                                actor.isAdmin())));
    }

    @Override
    @PreAuthorize("@gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<Void> removeGymStaff(
            @PathVariable("gymId") Integer gymId, @PathVariable("userId") Integer userId) {
        gymUseCase.removeGymStaff(gymId, userId, AuthenticatedUser.current().isAdmin());
        return ResponseEntity.noContent().build();
    }

    private GymDTO buildGymDto(CreateGymRequest createGymRequest) {
        GymDTO gym = new GymDTO();
        gym.setName(createGymRequest.getName());
        gym.setLocation(createGymRequest.getLocation());
        gym.setDescription(createGymRequest.getDescription());
        gym.setCreatorId(AuthenticatedUser.current().getId());
        gym.setTimeZone(createGymRequest.getTimeZone());
        gym.setLogo(createGymRequest.getLogo());
        return gym;
    }

    private GymDTO buildGymDto(Integer gymId, UpdateGymRequest updateGymRequest) {
        GymDTO gym = new GymDTO();
        gym.setId(gymId);
        gym.setDescription(updateGymRequest.getDescription());
        gym.setLocation(updateGymRequest.getLocation());
        gym.setName(updateGymRequest.getName());
        gym.setTimeZone(updateGymRequest.getTimeZone());
        gym.setLogo(updateGymRequest.getLogo());
        return gym;
    }
}
