package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.mapper.BoulderMapper;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.infrastructure.entity.BoulderEntity;
import com.climbingapp.infrastructure.repository.BoulderJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class BoulderRepositoryImpl implements BoulderRepository {

    @Autowired private BoulderJPARepository jpaRepository;

    @Autowired private BoulderMapper mapper;

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
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndCreatorId(Integer id, Integer creatorId) {
        return jpaRepository.existsByIdAndCreatorId(id, creatorId);
    }
}
