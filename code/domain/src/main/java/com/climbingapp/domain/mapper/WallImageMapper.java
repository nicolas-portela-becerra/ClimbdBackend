package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.infrastructure.entity.WallImageEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WallImageMapper {

    @Mapping(target = "gymId", source = "entity.gym.id")
    @Mapping(target = "uploadedBy", source = "entity.uploadedBy.id")
    WallImageDTO toDto(WallImageEntity entity);

    @Mapping(target = "gym", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    WallImageEntity toEntity(WallImageDTO dto);

    List<WallImageDTO> toDtoList(List<WallImageEntity> entities);

    List<WallImageEntity> toEntityList(List<WallImageDTO> dtos);
}
