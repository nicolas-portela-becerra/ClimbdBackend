package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.mapper.GymMapper;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.infrastructure.entity.GymEntity;
import com.climbingapp.infrastructure.repository.GymJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class GymRepositoryImpl implements GymRepository {

    @Autowired private GymJPARepository jpaRepository;

    @Autowired private GymMapper mapper;

    @Override
    public GymDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public GymDTO save(GymDTO gym) {
        GymEntity entity = mapper.toEntity(gym);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public Page<GymDTO> findAll(Pageable pageable) {
        return jpaRepository.findAll(pageable).map(mapper::toDto);
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }
}
