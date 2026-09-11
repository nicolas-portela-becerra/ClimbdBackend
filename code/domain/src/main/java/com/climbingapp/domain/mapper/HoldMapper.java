package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.infrastructure.entity.HoldEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HoldMapper {

    @Mapping(target = "boulderId", source = "entity.boulder.id")
    HoldDTO toDto(HoldEntity entity);

    @Mapping(target = "boulder", ignore = true)
    HoldEntity toEntity(HoldDTO dto);

    List<HoldDTO> toDtoList(List<HoldEntity> entities);

    List<HoldEntity> toEntityList(List<HoldDTO> dtos);
}
