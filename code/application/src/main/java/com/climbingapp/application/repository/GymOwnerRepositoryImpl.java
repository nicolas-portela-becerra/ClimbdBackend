package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.GymOwnerDTO;
import com.climbingapp.domain.mapper.GymOwnerMapper;
import com.climbingapp.domain.repository.GymOwnerRepository;
import com.climbingapp.infrastructure.entity.GymEntity;
import com.climbingapp.infrastructure.entity.GymOwnerEntity;
import com.climbingapp.infrastructure.entity.UserEntity;
import com.climbingapp.infrastructure.repository.GymOwnerJPARepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GymOwnerRepositoryImpl implements GymOwnerRepository {

    @Autowired private GymOwnerJPARepository jpaRepository;

    @Autowired private GymOwnerMapper mapper;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public boolean existsByGymIdAndUserId(Integer gymId, Integer userId) {
        return jpaRepository.existsByGymIdAndUserId(gymId, userId);
    }

    @Override
    public List<GymOwnerDTO> findByGymId(Integer gymId) {
        return mapper.toDtoList(jpaRepository.findByGymId(gymId));
    }

    @Override
    public GymOwnerDTO save(GymOwnerDTO gymOwner) {
        GymOwnerEntity entity = mapper.toEntity(gymOwner);
        if (gymOwner.getUserId() != null) {
            entity.setUser(entityManager.getReference(UserEntity.class, gymOwner.getUserId()));
        }
        if (gymOwner.getGymId() != null) {
            entity.setGym(entityManager.getReference(GymEntity.class, gymOwner.getGymId()));
        }
        if (gymOwner.getAssignedBy() != null) {
            entity.setAssignedBy(
                    entityManager.getReference(UserEntity.class, gymOwner.getAssignedBy()));
        }
        return mapper.toDto(jpaRepository.save(entity));
    }
}
