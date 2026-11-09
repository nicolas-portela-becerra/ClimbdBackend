package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.GymStaffDTO;
import com.climbingapp.domain.mapper.GymStaffMapper;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.infrastructure.entity.GymStaffEntity;
import com.climbingapp.infrastructure.entity.enums.GymRole;
import com.climbingapp.infrastructure.repository.GymStaffJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GymStaffRepositoryImpl implements GymStaffRepository {

    @Autowired private GymStaffJPARepository jpaRepository;

    @Autowired private GymStaffMapper mapper;

    @Override
    public GymRole findRole(Integer gymId, Integer userId) {
        if (gymId == null || userId == null) {
            return null;
        }
        return jpaRepository
                .findByGymIdAndUserId(gymId, userId)
                .map(GymStaffEntity::getRole)
                .orElse(null);
    }

    @Override
    public GymStaffDTO findByGymIdAndUserId(Integer gymId, Integer userId) {
        return jpaRepository.findByGymIdAndUserId(gymId, userId).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<GymStaffDTO> findByGymId(Integer gymId) {
        return mapper.toDtoList(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<GymStaffDTO> findByUserId(Integer userId) {
        return mapper.toDtoList(jpaRepository.findByUserId(userId));
    }

    @Override
    public GymStaffDTO save(GymStaffDTO gymStaff) {
        GymStaffEntity entity = mapper.toEntity(gymStaff);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }
}
