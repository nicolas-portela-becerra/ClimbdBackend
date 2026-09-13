package com.climbingapp.api.controller;

import com.climbingapp.api.dto.WallImageDetail;
import com.climbingapp.api.dto.WallImagePageResponse;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.usecase.GymUseCase;
import com.climbingapp.domain.usecase.WallImageUseCase;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@RestController
public class WallImageController implements WallImagesApi {

    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES =
            Set.of("image/jpeg", "image/png", "image/webp");

    @Autowired private WallImageUseCase wallImageUseCase;

    @Autowired private GymUseCase gymUseCase;

    @Autowired private UserRepository userRepository;

    @Autowired private ApiDtoMapper mapper;

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<WallImagePageResponse> listWallImages(
            @PathVariable("gymId") Integer gymId, Integer page, Integer size) {
        return ResponseEntity.ok(
                mapper.toWallImagePage(
                        wallImageUseCase.getAllWallImages(gymId, PageRequest.of(page, size))));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @gymSecurity.isOwner(#gymId, authentication)")
    public ResponseEntity<Void> uploadWallImage(
            @PathVariable("gymId") Integer gymId,
            MultipartFile image,
            String wallName,
            Integer uploadedBy) {
        if (gymUseCase.getGymById(gymId) == null) {
            throw new NotFoundException("Gym with id " + gymId + " does not exist.");
        }
        validateImage(image);
        try {
            WallImageDTO wallImage = buildImage(gymId, image, wallName, uploadedBy);
            wallImageUseCase.uploadWallImage(wallImage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<WallImageDetail> getWallImageById(
            @PathVariable("wallImageId") Integer wallImageId) {
        WallImageDTO wallImage = wallImageUseCase.getWallImageById(wallImageId);
        if (wallImage == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(mapper.toWallImageDetail(wallImage));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Resource> getWallImageData(
            @PathVariable("wallImageId") Integer wallImageId) {
        WallImageDTO wallImage = wallImageUseCase.getWallImageById(wallImageId);
        if (wallImage == null || wallImage.getImageData() == null) {
            return ResponseEntity.notFound().build();
        }
        return imageResponse(wallImage.getImageData(), wallImage.getMimeType());
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Resource> getWallImageThumbnail(
            @PathVariable("wallImageId") Integer wallImageId) {
        WallImageDTO wallImage = wallImageUseCase.getWallImageById(wallImageId);
        if (wallImage == null || wallImage.getThumbnail() == null) {
            return ResponseEntity.notFound().build();
        }
        return imageResponse(wallImage.getThumbnail(), MediaType.IMAGE_JPEG_VALUE);
    }

    private WallImageDTO buildImage(
            Integer gymId, MultipartFile image, String wallName, Integer uploadedBy) {
        WallImageDTO wallImage = new WallImageDTO();
        wallImage.setGymId(gymId);
        wallImage.setWallName(wallName.trim());
        wallImage.setImageData(readBytes(image));
        wallImage.setThumbnail(
                new byte[0]); // placeholder — the usecase generates the real thumbnail
        wallImage.setMimeType(
                image.getContentType()); // overwritten to image/jpeg by the usecase after
        // compression
        wallImage.setUploadedBy(uploadedBy);
        wallImage.setUploadedDate(LocalDateTime.now());
        return wallImage;
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        if (!ALLOWED_TYPES.contains(image.getContentType())) {
            throw new IllegalArgumentException("Unsupported image type: " + image.getContentType());
        }
        if (image.getSize() > MAX_IMAGE_BYTES) {
            throw new IllegalArgumentException("Image exceeds the 5MB limit");
        }
    }

    private byte[] readBytes(MultipartFile image) {
        try {
            return image.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not read image file", e);
        }
    }

    private ResponseEntity<Resource> imageResponse(byte[] data, String mimeType) {
        return ResponseEntity.ok()
                .contentType(
                        MediaType.parseMediaType(
                                mimeType != null ? mimeType : MediaType.IMAGE_JPEG_VALUE))
                .contentLength(data.length)
                .body(new ByteArrayResource(data));
    }
}
