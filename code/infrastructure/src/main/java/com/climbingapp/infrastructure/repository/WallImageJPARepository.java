package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.WallImageEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface WallImageJPARepository extends JpaRepository<WallImageEntity, Integer> {

    // List view only needs metadata: skip the image_data/thumbnail blobs.
    @Query(
            """
            SELECT new com.climbingapp.infrastructure.entity.WallImageEntity(w.id, w.gymId, w.wallName, w.mimeType, w.widthPx,
                w.heightPx, w.uploadedBy, w.uploadedDate)
            FROM WallImageEntity w WHERE w.gymId = :gymId
            """)
    Page<WallImageEntity> findSummariesByGymId(Integer gymId, Pageable pageable);

    Optional<WallImageEntity> findTopByGymIdAndWallNameOrderByUploadedDateDesc(
            Integer gymId, String wallName);
}
