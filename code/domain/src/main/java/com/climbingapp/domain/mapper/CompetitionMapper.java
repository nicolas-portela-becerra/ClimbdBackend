package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.infrastructure.entity.CompetitionEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetitionMapper {

    CompetitionDTO toDto(CompetitionEntity entity);

    CompetitionEntity toEntity(CompetitionDTO dto);

    List<CompetitionDTO> toDtoList(List<CompetitionEntity> entities);

    List<CompetitionEntity> toEntityList(List<CompetitionDTO> dtos);
}
