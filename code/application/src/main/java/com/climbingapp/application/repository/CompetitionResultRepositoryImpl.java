package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.CompetitionRankingRecord;
import com.climbingapp.domain.dto.CompetitionResultDTO;
import com.climbingapp.domain.mapper.CompetitionResultMapper;
import com.climbingapp.domain.repository.CompetitionResultRepository;
import com.climbingapp.infrastructure.entity.CompetitionResultEntity;
import com.climbingapp.infrastructure.repository.CompetitionResultJPARepository;
import com.climbingapp.infrastructure.view.CompetitionRankingView;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class CompetitionResultRepositoryImpl implements CompetitionResultRepository {

    @Autowired private CompetitionResultJPARepository jpaRepository;

    @Autowired private CompetitionResultMapper mapper;

    @Override
    public CompetitionResultDTO findById(Integer id) {
        return jpaRepository.findById(id).map(mapper::toDto).orElse(null);
    }

    @Override
    public List<CompetitionResultDTO> findByCompetitionId(Integer competitionId) {
        return mapper.toDtoList(jpaRepository.findByCompetitionId(competitionId));
    }

    @Override
    public List<CompetitionResultDTO> findByParticipantId(Integer participantId) {
        return mapper.toDtoList(jpaRepository.findByParticipantId(participantId));
    }

    @Override
    public CompetitionResultDTO findByParticipantIdAndBoulderId(
            Integer participantId, Integer boulderId) {
        return jpaRepository
                .findByParticipantIdAndBoulderId(participantId, boulderId)
                .map(mapper::toDto)
                .orElse(null);
    }

    @Override
    public void upsertResults(
            Integer participantId, Integer competitionId, List<CompetitionResultDTO> results) {
        if (results.isEmpty()) {
            return;
        }
        jpaRepository.upsertResults(
                participantId,
                competitionId,
                results.stream().map(CompetitionResultDTO::getBoulderId).toArray(Integer[]::new),
                results.stream().map(CompetitionResultDTO::getAttempts).toArray(Integer[]::new),
                results.stream().map(CompetitionResultDTO::isTopped).toArray(Boolean[]::new));
    }

    @Override
    public CompetitionResultDTO save(CompetitionResultDTO result) {
        CompetitionResultEntity entity = mapper.toEntity(result);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public void deleteById(Integer id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<CompetitionRankingRecord> getCompetitionRanking(Integer competitionId, Timestamp now) {
        List<CompetitionRankingView> ranking =
                jpaRepository.getFinalCompetitionResults(competitionId, now);
        return ranking.stream()
                .map(
                        row ->
                                new CompetitionRankingRecord(
                                        row.getName(),
                                        row.getSection(),
                                        row.getGender(),
                                        row.getTotalPoints(),
                                        row.getTotalAttempts(),
                                        row.getFlashTops()))
                .toList();
    }
}
