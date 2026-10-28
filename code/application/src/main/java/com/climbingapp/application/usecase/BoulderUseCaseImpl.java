package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.BoulderResponseDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.mapper.BoulderMapper;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.HoldRepository;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.BoulderUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BoulderUseCaseImpl implements BoulderUseCase {

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private HoldRepository holdRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private BoulderMapper boulderMapper;

    @Override
    @Transactional
    public BoulderDTO createBoulder(BoulderDTO boulderDTO, List<HoldDTO> holds) {
        try {
            BoulderDTO savedBoulder = boulderRepository.save(boulderDTO);
            log.info("Boulder created: {}", savedBoulder);
            holds.forEach(hold -> hold.setBoulderId(savedBoulder.getId()));
            holdRepository.saveAll(holds);
            log.info(
                    "{} holds created for boulder with id: {}", holds.size(), savedBoulder.getId());
            return savedBoulder;
        } catch (Exception e) {
            log.info("Boulder could not be saved: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public BoulderDTO getBoulderById(int id) {
        try {
            return boulderRepository.findById(id);
        } catch (Exception e) {
            log.error("Error finding boulder by id: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    public List<HoldDTO> getBoulderHolds(int boulderId) {
        return holdRepository.findByBoulderIdOrderBySequenceOrder(boulderId);
    }

    @Override
    public Page<BoulderDTO> getAllBoulders(int gymId, Pageable pageable) {
        try {
            return boulderRepository.findByGymId(gymId, pageable);
        } catch (Exception e) {
            log.error("Error retrieving boulders for gym with id {}: {}", gymId, e.getMessage());
            throw e;
        }
    }

    @Override
    public Page<BoulderResponseDTO> getBouldersByWallImage(int wallImageId, Pageable pageable) {
        try {
            Page<BoulderDTO> page = boulderRepository.findByWallImageId(wallImageId, pageable);
            List<BoulderResponseDTO> boulderResponseDTOList = addCreatorToBoulder(page);
            return new PageImpl<>(boulderResponseDTOList, pageable, page.getTotalElements());
        } catch (Exception e) {
            log.error(
                    "Error retrieving boulders for wall image with id {}: {}",
                    wallImageId,
                    e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public BoulderDTO updateBoulder(BoulderDTO boulderDTO, List<HoldDTO> holds) {
        try {
            BoulderDTO currentBoulder = boulderRepository.findById(boulderDTO.getId());
            BoulderDTO updatedBoulder = null;
            List<HoldDTO> currentHolds =
                    holdRepository.findByBoulderIdOrderBySequenceOrder(boulderDTO.getId());

            if (!ObjectUtils.nullSafeEquals(currentBoulder, boulderDTO)) {
                updatedBoulder = updateBoulderDto(boulderDTO, currentBoulder);
                log.debug("Boulder {} updated", currentBoulder.getId());
            } else if (!ObjectUtils.isEmpty(currentHolds) && holds.size() != currentHolds.size()) {
                updateHoldsDto(holds, currentHolds);
                updatedBoulder = currentBoulder;
                log.debug("Holds updated for boulder: {}", currentBoulder.getId());
            } else {
                updatedBoulder = new BoulderDTO();
                log.info("Nothing to update in the boulder: {}", boulderDTO);
            }
            return updatedBoulder;
        } catch (Exception e) {
            log.error("Error updating boulder: {}", e.getMessage());
            throw e;
        }
    }

    private void updateHoldsDto(List<HoldDTO> holds, List<HoldDTO> currentHolds) {
        if (holds != null && !holds.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            holds.forEach(
                    hold -> {
                        hold.setUpdatedDate(now);
                    });
            holdRepository.deleteByBoulderId(currentHolds.getFirst().getBoulderId());
            holdRepository.saveAll(holds);
        }
    }

    private BoulderDTO updateBoulderDto(BoulderDTO newBoulder, BoulderDTO currentBoulder) {
        currentBoulder.setName(newBoulder.getName());
        currentBoulder.setDescription(newBoulder.getDescription());
        currentBoulder.setGrade(newBoulder.getGrade());
        currentBoulder.setUpdatedDate(LocalDateTime.now());
        return boulderRepository.save(currentBoulder);
    }

    @Override
    @Transactional
    public void deleteBoulder(int id) {
        try {
            holdRepository.deleteByBoulderId(id);
            boulderRepository.deleteById(id);
            log.info("Boulder with id {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting boulder with id {}: {}", id, e.getMessage());
            throw e;
        }
    }

    private List<BoulderResponseDTO> addCreatorToBoulder(Page<BoulderDTO> boulders) {
        return boulders.getContent().stream()
                .map(
                        boulder -> {
                            UserDTO creator = userRepository.findById(boulder.getCreatorId());
                            return boulderMapper.toResponseDTO(boulder, creator);
                        })
                .toList();
    }
}
