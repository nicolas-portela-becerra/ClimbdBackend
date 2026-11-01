package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.CompetitionParticipantDTO;
import com.climbingapp.infrastructure.entity.CompetitionParticipantEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CompetitionParticipantMapper {

    CompetitionParticipantDTO toDto(CompetitionParticipantEntity entity);

    CompetitionParticipantEntity toEntity(CompetitionParticipantDTO dto);

    List<CompetitionParticipantDTO> toDtoList(List<CompetitionParticipantEntity> entities);

    List<CompetitionParticipantEntity> toEntityList(List<CompetitionParticipantDTO> dtos);
}
