package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.mapper.CompetitionMapper;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.infrastructure.entity.CompetitionEntity;
import com.climbingapp.infrastructure.repository.CompetitionJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class CompetitionRepositoryImpl implements CompetitionRepository {

    @Autowired private CompetitionJPARepository jpaRepository;

    @Autowired private CompetitionMapper mapper;

    @Override
    public CompetitionDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public Page<CompetitionDTO> findByGymId(Integer gymId, Pageable pageable) {
        return jpaRepository.findByGymId(gymId, pageable).map(mapper::toDto);
    }

    @Override
    public CompetitionDTO save(CompetitionDTO competition) {
        CompetitionEntity entity = mapper.toEntity(competition);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByIdAndGymId(Integer id, Integer gymId) {
        return jpaRepository.existsByIdAndGymId(id, gymId);
    }

    @Override
    public List<CompetitionDTO> findNotEndedByGymId(Integer gymId, Instant endedAfter) {
        return mapper.toDtoList(
                jpaRepository.findByGymIdAndDateToAfterOrderByDateFromAsc(gymId, endedAfter));
    }
}
