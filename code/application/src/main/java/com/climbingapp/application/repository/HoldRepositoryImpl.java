package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.mapper.HoldMapper;
import com.climbingapp.domain.repository.HoldRepository;
import com.climbingapp.infrastructure.entity.HoldEntity;
import com.climbingapp.infrastructure.repository.HoldJPARepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class HoldRepositoryImpl implements HoldRepository {

    @Autowired private HoldJPARepository jpaRepository;

    @Autowired private HoldMapper mapper;

    @PersistenceContext private EntityManager entityManager;

    @Override
    public List<HoldDTO> findByBoulderIdOrderBySequenceOrder(Integer boulderId) {
        return mapper.toDtoList(jpaRepository.findByBoulderIdOrderBySequenceOrder(boulderId));
    }

    @Override
    public List<HoldDTO> saveAll(List<HoldDTO> holds) {
        List<HoldEntity> entities = mapper.toEntityList(holds);
        return mapper.toDtoList(jpaRepository.saveAll(entities));
    }

    @Override
    public void deleteByBoulderId(Integer boulderId) {
        jpaRepository.deleteByBoulderId(boulderId);
    }
}
