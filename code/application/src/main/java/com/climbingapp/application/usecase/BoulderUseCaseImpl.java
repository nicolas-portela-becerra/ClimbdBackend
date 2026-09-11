package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.HoldRepository;
import com.climbingapp.domain.usecase.BoulderUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BoulderUseCaseImpl implements BoulderUseCase {

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private HoldRepository holdRepository;

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
    public Page<BoulderDTO> getBouldersByWallImage(int wallImageId, Pageable pageable) {
        try {
            return boulderRepository.findByWallImageId(wallImageId, pageable);
        } catch (Exception e) {
            log.error(
                    "Error retrieving boulders for wall image with id {}: {}",
                    wallImageId,
                    e.getMessage());
            throw e;
        }
    }

    @Override
    public BoulderDTO updateBoulder(BoulderDTO boulderDTO) {
        try {
            BoulderDTO currentBoulder = boulderRepository.findById(boulderDTO.getId());
            BoulderDTO updatedBoulder = null;
            if (currentBoulder != null && !currentBoulder.equals(boulderDTO)) {
                boulderDTO.setUpdatedDate(LocalDateTime.now());
                currentBoulder.setDescription(boulderDTO.getDescription());
                currentBoulder.setGrade(boulderDTO.getGrade());
                currentBoulder.setGymId(boulderDTO.getGymId());
                updatedBoulder = boulderRepository.save(currentBoulder);
            } else if (currentBoulder != null && currentBoulder.equals(boulderDTO)) {
                updatedBoulder = new BoulderDTO();
            }
            return updatedBoulder;
        } catch (Exception e) {
            log.error("Error updating boulder: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteBoulder(int id) {
        try {
            boulderRepository.deleteById(id);
            log.info("Boulder with id {} deleted successfully", id);
        } catch (Exception e) {
            log.error("Error deleting boulder with id {}: {}", id, e.getMessage());
            throw e;
        }
    }
}
