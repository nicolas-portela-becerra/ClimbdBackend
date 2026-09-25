package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.infrastructure.entity.WallImageEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WallImageMapper {

    WallImageDTO toDto(WallImageEntity entity);

    WallImageEntity toEntity(WallImageDTO dto);

    List<WallImageDTO> toDtoList(List<WallImageEntity> entities);

    List<WallImageEntity> toEntityList(List<WallImageDTO> dtos);
}
