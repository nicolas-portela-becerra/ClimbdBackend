package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.CompetitionResultDTO;
import com.climbingapp.infrastructure.entity.CompetitionResultEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetitionResultMapper {

    CompetitionResultDTO toDto(CompetitionResultEntity entity);

    CompetitionResultEntity toEntity(CompetitionResultDTO dto);

    List<CompetitionResultDTO> toDtoList(List<CompetitionResultEntity> entities);

    List<CompetitionResultEntity> toEntityList(List<CompetitionResultDTO> dtos);
}
