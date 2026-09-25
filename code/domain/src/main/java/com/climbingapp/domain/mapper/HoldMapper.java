package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.infrastructure.entity.HoldEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HoldMapper {

    HoldDTO toDto(HoldEntity entity);

    HoldEntity toEntity(HoldDTO dto);

    List<HoldDTO> toDtoList(List<HoldEntity> entities);

    List<HoldEntity> toEntityList(List<HoldDTO> dtos);
}
