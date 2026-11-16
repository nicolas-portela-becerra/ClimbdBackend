package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.CompetitionRankingDTO;
import com.climbingapp.domain.mapper.CompetitionRankingMapper;
import com.climbingapp.domain.repository.CompetitionRankingRepository;
import com.climbingapp.infrastructure.repository.CompetitionRankingJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public class CompetitionRankingRepositoryImpl implements CompetitionRankingRepository {

    @Autowired private CompetitionRankingJPARepository jpaRepository;

    @Autowired private CompetitionRankingMapper mapper;

    @Override
    public List<CompetitionRankingDTO> findByCompetitionId(Integer competitionId) {
        return mapper.toDtoList(
                jpaRepository
                        .findByCompetitionIdOrderByTotalPointsDescFlashTopsDescTotalAttemptsAsc(
                                competitionId));
    }

    @Override
    public int saveFinalRanking(Collection<Integer> competitionIds) {
        return jpaRepository.saveFinalRanking(competitionIds);
    }
}
