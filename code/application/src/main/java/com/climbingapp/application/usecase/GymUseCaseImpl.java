package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.GymOwnerDTO;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.GymOwnerRepository;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.GymUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class GymUseCaseImpl implements GymUseCase {

    @Autowired private GymRepository gymRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private GymOwnerRepository gymOwnerRepository;

    @Override
    public GymDTO createGym(GymDTO gymDTO) {
        try {
            return gymRepository.save(gymDTO);
        } catch (Exception e) {
            log.error("Error creating gym: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public GymDTO getGymById(int id) {
        try {
            return gymRepository.findById(id);
        } catch (Exception e) {
            log.error("Error getting gym by id: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public Page<GymDTO> getAllGyms(Pageable pageable) {
        try {
            return gymRepository.findAll(pageable);
        } catch (Exception e) {
            log.error("Error getting gyms: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public GymDTO updateGym(GymDTO newGymDTO) {
        try {
            GymDTO currentGym = gymRepository.findById(newGymDTO.getId());

            if (currentGym == null) return currentGym;

            if (!newGymDTO.equals(currentGym)) {
                currentGym = gymRepository.save(newGymDTO);
            }
            return currentGym;
        } catch (Exception e) {
            log.error("Error updating gym: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public void assignGymOwner(int gymId, int userId, int assigneeId) {
        try {
            if (gymRepository.findById(gymId).getId() == null) {
                throw new NotFoundException("Gym not found");
            }
            if (userRepository.findById(userId).getId() == null) {
                throw new NotFoundException("User not found");
            }
            GymOwnerDTO gymOwnerDTO = buildGymOwner(gymId, userId, assigneeId);
            gymOwnerRepository.save(gymOwnerDTO);
        } catch (Exception e) {
            log.error("Error assigning gym owner: {}", e.getMessage());
            throw e;
        }
    }

    private GymOwnerDTO buildGymOwner(int gymId, int userId, int assigneeId) {
        GymOwnerDTO gymOwnerDTO = new GymOwnerDTO();
        gymOwnerDTO.setGymId(gymId);
        gymOwnerDTO.setUserId(userId);
        gymOwnerDTO.setAssignedBy(assigneeId);
        gymOwnerDTO.setAssignedAt(LocalDateTime.now());
        return gymOwnerDTO;
    }
}
