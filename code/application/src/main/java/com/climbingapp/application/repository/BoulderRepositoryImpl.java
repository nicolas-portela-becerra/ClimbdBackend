package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.mapper.BoulderMapper;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.infrastructure.entity.BoulderEntity;
import com.climbingapp.infrastructure.entity.GymEntity;
import com.climbingapp.infrastructure.entity.UserEntity;
import com.climbingapp.infrastructure.entity.WallImageEntity;
import com.climbingapp.infrastructure.repository.BoulderJPARepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class BoulderRepositoryImpl implements BoulderRepository {

    @Autowired private BoulderJPARepository jpaRepository;

    @Autowired private BoulderMapper mapper;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public BoulderDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public Page<BoulderDTO> findByGymId(Integer gymId, Pageable pageable) {
        return jpaRepository.findByGymId(gymId, pageable).map(mapper::toDto);
    }

    @Override
    public Page<BoulderDTO> findByWallImageId(Integer wallImageId, Pageable pageable) {
        return jpaRepository.findByWallImageId(wallImageId, pageable).map(mapper::toDto);
    }

    @Override
    public BoulderDTO save(BoulderDTO boulder) {
        BoulderEntity entity = mapper.toEntity(boulder);
        if (boulder.getGymId() != null) {
            entity.setGym(entityManager.getReference(GymEntity.class, boulder.getGymId()));
        }
        if (boulder.getWallImageId() != null) {
            entity.setWallImage(
                    entityManager.getReference(WallImageEntity.class, boulder.getWallImageId()));
        }
        if (boulder.getCreatorId() != null) {
            entity.setCreator(entityManager.getReference(UserEntity.class, boulder.getCreatorId()));
        }
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndCreatorEmail(Integer id, String email) {
        return jpaRepository.existsByIdAndCreatorEmail(id, email);
    }
}
