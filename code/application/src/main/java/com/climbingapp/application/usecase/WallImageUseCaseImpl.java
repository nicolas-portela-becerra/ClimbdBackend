package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.repository.WallImageRepository;
import com.climbingapp.domain.usecase.ImageProcessorUseCase;
import com.climbingapp.domain.usecase.WallImageUseCase;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class WallImageUseCaseImpl implements WallImageUseCase {

    @Autowired private WallImageRepository wallImageRepository;

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
