package com.climbingapp.api.controller;

import com.climbingapp.api.dto.BoulderDetailDto;
import com.climbingapp.api.dto.BoulderDto;
import com.climbingapp.api.dto.BoulderPageResponse;
import com.climbingapp.api.dto.CreateBoulderRequest;
import com.climbingapp.api.dto.CreateHoldRequest;
import com.climbingapp.api.dto.UpdateBoulderRequest;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.BoulderUseCase;
import com.climbingapp.domain.usecase.WallImageUseCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
public class BoulderController implements BouldersApi {

    @Autowired private BoulderUseCase boulderUseCase;

    @Autowired private WallImageUseCase wallImageUseCase;

    @Autowired private UserRepository userRepository;

    @Autowired private ApiDtoMapper mapper;

    @Override
    public ResponseEntity<BoulderPageResponse> listBoulders(
            @PathVariable("gymId") Integer gymId, Integer page, Integer size) {
        return ResponseEntity.ok(
                mapper.toBoulderPage(
                        boulderUseCase.getAllBoulders(gymId, PageRequest.of(page, size))));
    }

    @Override
    public ResponseEntity<BoulderPageResponse> listBouldersByWall(
            @PathVariable("wallImageId") Integer wallImageId, Integer page, Integer size) {
        if (wallImageUseCase.getWallImageById(wallImageId) == null) {
            throw new NotFoundException("Wall image with id " + wallImageId + " does not exist.");
        }
        return ResponseEntity.ok(
                mapper.toBoulderPage(
                        boulderUseCase.getBouldersByWallImage(
                                wallImageId, PageRequest.of(page, size))));
    }

    @Override
    public ResponseEntity<BoulderDto> createBoulder(
            @PathVariable("gymId") Integer gymId, CreateBoulderRequest createBoulderRequest) {
        // Safety check in case someone tries to create a boulder in a wall that does not match the
        // gym or does not exist
        WallImageDTO wallImage =
                wallImageUseCase.getWallImageById(createBoulderRequest.getWallImageId());
        if (wallImage == null || !gymId.equals(wallImage.getGymId())) {
            throw new IllegalArgumentException(
                    "wallImageId does not reference a wall image of gym " + gymId);
        }

        BoulderDTO boulder = buildBoulder(gymId, createBoulderRequest);

        List<CreateHoldRequest> requestHolds =
                createBoulderRequest.getHolds() != null
                        ? createBoulderRequest.getHolds()
                        : List.of();
        List<HoldDTO> holds = new ArrayList<>(requestHolds.size());
        // TODO: hold order does not matter at all, remove the sequence property
        for (int i = 0; i < requestHolds.size(); i++) {
            HoldDTO hold = mapper.toDomainHold(requestHolds.get(i));
            if (hold.getSequenceOrder() == null) {
                hold.setSequenceOrder(i);
            }
            hold.setCreatedDate(LocalDateTime.now());
            holds.add(hold);
        }

        BoulderDTO saved = boulderUseCase.createBoulder(boulder, holds);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toApiBoulder(saved));
    }

    @Override
    public ResponseEntity<BoulderDetailDto> getBoulderById(
            @PathVariable("boulderId") Integer boulderId) {
        BoulderDTO boulder = boulderUseCase.getBoulderById(boulderId);
        if (boulder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(
                mapper.toBoulderDetail(boulder, boulderUseCase.getBoulderHolds(boulderId)));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @boulderSecurity.isCreator(#boulderId, authentication)")
    public ResponseEntity<BoulderDto> updateBoulder(
            @PathVariable("boulderId") Integer boulderId,
            UpdateBoulderRequest updateBoulderRequest) {
        BoulderDTO boulderDto = buildBoulder(boulderId, updateBoulderRequest);
        BoulderDTO updatedBoulder = boulderUseCase.updateBoulder(boulderDto);
        if (updatedBoulder == null) {
            return ResponseEntity.notFound().build();
        } else {
            return ResponseEntity.ok(mapper.toApiBoulder(updatedBoulder));
        }
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @boulderSecurity.isCreator(#boulderId, authentication)")
    public ResponseEntity<Void> deleteBoulder(@PathVariable("boulderId") Integer boulderId) {
        if (boulderUseCase.getBoulderById(boulderId) == null) {
            return ResponseEntity.notFound().build();
        }
        boulderUseCase.deleteBoulder(boulderId);
        return ResponseEntity.noContent().build();
    }

    private BoulderDTO buildBoulder(Integer gymId, CreateBoulderRequest createBoulderRequest) {
        BoulderDTO boulder = new BoulderDTO();
        boulder.setGymId(gymId);
        boulder.setWallImageId(createBoulderRequest.getWallImageId());
        boulder.setCreatorId(createBoulderRequest.getCreatedBy());
        boulder.setName(createBoulderRequest.getName());
        boulder.setGrade(createBoulderRequest.getGrade());
        boulder.setDescription(createBoulderRequest.getDescription());
        boulder.setCreatedDate(LocalDateTime.now());
        return boulder;
    }

    private BoulderDTO buildBoulder(Integer boulderId, UpdateBoulderRequest updateBoulderRequest) {
        BoulderDTO boulder = new BoulderDTO();
        boulder.setId(boulderId);
        boulder.setDescription(updateBoulderRequest.getDescription());
        boulder.setName(updateBoulderRequest.getName());
        boulder.setGrade(updateBoulderRequest.getGrade());
        return boulder;
    }
}
