package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.mapper.WallImageMapper;
import com.climbingapp.domain.repository.WallImageRepository;
import com.climbingapp.infrastructure.entity.WallImageEntity;
import com.climbingapp.infrastructure.repository.WallImageJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class WallImageRepositoryImpl implements WallImageRepository {

    @Autowired private WallImageJPARepository jpaRepository;

    @Autowired private WallImageMapper mapper;

    @Override
    public WallImageDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public Page<WallImageDTO> findByGymId(Integer gymId, Pageable pageable) {
        return jpaRepository.findByGymId(gymId, pageable).map(mapper::toDto);
    }

    @Override
    public WallImageDTO save(WallImageDTO wallImage) {
        WallImageEntity entity = mapper.toEntity(wallImage);
        return mapper.toDto(jpaRepository.save(entity));
    }
}
