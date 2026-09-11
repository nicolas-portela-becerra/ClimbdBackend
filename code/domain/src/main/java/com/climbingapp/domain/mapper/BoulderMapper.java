package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.infrastructure.entity.BoulderEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoulderMapper {

    @Mapping(target = "gymId", source = "entity.gym.id")
    @Mapping(target = "wallImageId", source = "entity.wallImage.id")
    @Mapping(target = "creatorId", source = "entity.creator.id")
    @Mapping(target = "wallActual", source = "entity.wallImage.isActual")
    BoulderDTO toDto(BoulderEntity entity);

    @Mapping(target = "gym", ignore = true)
    @Mapping(target = "wallImage", ignore = true)
    @Mapping(target = "creator", ignore = true)
    BoulderEntity toEntity(BoulderDTO dto);

    List<BoulderDTO> toDtoList(List<BoulderEntity> entities);

    List<BoulderEntity> toEntityList(List<BoulderDTO> dtos);
}
