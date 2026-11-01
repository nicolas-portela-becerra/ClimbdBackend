package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.CompetitionParticipantDTO;
import com.climbingapp.domain.mapper.CompetitionParticipantMapper;
import com.climbingapp.domain.repository.CompetitionParticipantRepository;
import com.climbingapp.infrastructure.entity.CompetitionParticipantEntity;
import com.climbingapp.infrastructure.repository.CompetitionParticipantJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CompetitionParticipantRepositoryImpl implements CompetitionParticipantRepository {

    @Autowired private CompetitionParticipantJPARepository jpaRepository;

    @Autowired private CompetitionParticipantMapper mapper;

    @Override
    public CompetitionParticipantDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<CompetitionParticipantDTO> findByCompetitionId(Integer competitionId) {
        return mapper.toDtoList(jpaRepository.findByCompetitionId(competitionId));
    }

    @Override
    public CompetitionParticipantDTO findByCompetitionIdAndUserId(
            Integer competitionId, Integer userId) {
        return jpaRepository
                .findByCompetitionIdAndUserId(competitionId, userId)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public boolean existsByCompetitionIdAndUserId(Integer competitionId, Integer userId) {
        return jpaRepository.existsByCompetitionIdAndUserId(competitionId, userId);
    }

    @Override
    public CompetitionParticipantDTO save(CompetitionParticipantDTO participant) {
        CompetitionParticipantEntity entity = mapper.toEntity(participant);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }
}
