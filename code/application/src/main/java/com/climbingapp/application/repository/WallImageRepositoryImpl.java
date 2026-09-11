package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.mapper.WallImageMapper;
import com.climbingapp.domain.repository.WallImageRepository;
import com.climbingapp.infrastructure.entity.GymEntity;
import com.climbingapp.infrastructure.entity.UserEntity;
import com.climbingapp.infrastructure.entity.WallImageEntity;
import com.climbingapp.infrastructure.repository.WallImageJPARepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class WallImageRepositoryImpl implements WallImageRepository {

    @Autowired private WallImageJPARepository jpaRepository;

    @Autowired private WallImageMapper mapper;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public WallImageDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public Page<WallImageDTO> findByGymId(Integer gymId, Pageable pageable) {
        return jpaRepository.findByGymId(gymId, pageable).map(mapper::toDto);
    }

    @Override
    public List<WallImageDTO> findByGymIdAndIsActualTrue(Integer gymId) {
        return mapper.toDtoList(jpaRepository.findByGymIdAndIsActualTrue(gymId));
    }

    @Override
    public WallImageDTO findTopByGymIdAndWallNameAndIsActualTrueOrderByUploadedDateDesc(
            Integer gymId, String wallName) {
        return jpaRepository
                .findTopByGymIdAndWallNameAndIsActualTrueOrderByUploadedDateDesc(gymId, wallName)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public WallImageDTO save(WallImageDTO wallImage) {
        WallImageEntity entity = mapper.toEntity(wallImage);
        if (wallImage.getGymId() != null) {
            entity.setGym(entityManager.getReference(GymEntity.class, wallImage.getGymId()));
        }
        if (wallImage.getUploadedBy() != null) {
            entity.setUploadedBy(
                    entityManager.getReference(UserEntity.class, wallImage.getUploadedBy()));
        }
        return mapper.toDto(jpaRepository.save(entity));
    }
}
