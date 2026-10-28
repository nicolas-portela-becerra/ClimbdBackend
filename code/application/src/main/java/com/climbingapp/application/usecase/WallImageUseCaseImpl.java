package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.HoldRepository;
import com.climbingapp.domain.repository.WallImageRepository;
import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.climbingapp.domain.usecase.WallImageUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class WallImageUseCaseImpl implements WallImageUseCase {

    @Autowired private WallImageRepository wallImageRepository;

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private HoldRepository holdRepository;

    @Autowired private ImageProcessorUseCase imageProcessorUseCase;

    @Override
    public WallImageDTO uploadWallImage(WallImageDTO wallImageDTO) {
        try {
            wallImageDTO.setImageData(
                    imageProcessorUseCase.compress(wallImageDTO.getImageData(), 1920));
            wallImageDTO.setThumbnail(
                    imageProcessorUseCase.createThumbnail(wallImageDTO.getImageData(), 300));
            wallImageDTO.setMimeType("image/jpeg");
            return wallImageRepository.save(wallImageDTO);
        } catch (IllegalArgumentException e) {
            log.error("Invalid wall image data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error uploading wall image: {}", e.getMessage());
            throw new RuntimeException("Failed to upload wall image", e);
        }
    }

    @Override
    @Transactional
    public WallImageDTO updateWallImage(Integer id, String wallName, byte[] newImageData) {
        WallImageDTO wallImage = wallImageRepository.findById(id);
        if (wallImage == null) {
            throw new NotFoundException("Wall image with id " + id + " does not exist.");
        }
        wallImage.setWallName(wallName);
        if (newImageData != null) {
            wallImage.setImageData(imageProcessorUseCase.compress(newImageData, 1920));
            wallImage.setThumbnail(
                    imageProcessorUseCase.createThumbnail(wallImage.getImageData(), 300));
            wallImage.setMimeType("image/jpeg");
            wallImage.setUploadedDate(LocalDateTime.now());
        }
        return wallImageRepository.save(wallImage);
    }

    @Override
    @Transactional
    public void deleteWallImage(Integer id) {
        List<BoulderDTO> boulders = boulderRepository.findByWallImageId(id);
        if (!ObjectUtils.isEmpty(boulders)){
            holdRepository.deleteByBoulderId(boulders.getFirst().getId());
            boulderRepository.deleteById(boulders.getFirst().getId());
        } else {
            log.info("No boulders to delete in the wall {}", id);
        }
        wallImageRepository.deleteById(id);
        log.info("Wall image with id {} deleted successfully", id);
    }

    @Override
    public WallImageDTO getWallImageById(Integer id) {
        try {
            return wallImageRepository.findById(id);
        } catch (Exception e) {
            log.error("Error retrieving wall image: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve wall image", e);
        }
    }

    @Override
    public byte[] getWallImageData(Integer id) {
        try {
            return wallImageRepository.findById(id).getImageData();
        } catch (Exception e) {
            log.error("Error retrieving wall image data: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve wall data", e);
        }
    }

    @Override
    public byte[] getWallImageThumbnail(Integer id) {
        try {
            return wallImageRepository.findById(id).getThumbnail();
        } catch (Exception e) {
            log.error("Error retrieving wall image thumbnail: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve wall image thumbnail", e);
        }
    }

    @Override
    public Page<WallImageDTO> getAllWallImages(Integer gymId, Pageable pageable) {
        try {
            return wallImageRepository.findByGymId(gymId, pageable);
        } catch (Exception e) {
            log.error("Error retrieving wall images by gym id: {}", e.getMessage());
            throw new RuntimeException("Failed to retrieve wall images", e);
        }
    }
}
