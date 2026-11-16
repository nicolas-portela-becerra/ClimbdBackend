package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.CompetitionRankingDTO;
import com.climbingapp.infrastructure.entity.CompetitionRankingEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetitionRankingMapper {

    CompetitionRankingDTO toDto(CompetitionRankingEntity entity);

    CompetitionRankingEntity toEntity(CompetitionRankingDTO dto);

    List<CompetitionRankingDTO> toDtoList(List<CompetitionRankingEntity> entities);
}
