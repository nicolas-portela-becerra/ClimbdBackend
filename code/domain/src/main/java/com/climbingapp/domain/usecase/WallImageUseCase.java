package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.WallImageDTO;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WallImageUseCase {

    WallImageDTO uploadWallImage(WallImageDTO wallImageDTO);

    WallImageDTO getWallImageById(Integer id);

    byte[] getWallImageData(Integer id);

    byte[] getWallImageThumbnail(Integer id);

    Page<WallImageDTO> getAllWallImages(Integer gymId, Pageable pageable);
}
