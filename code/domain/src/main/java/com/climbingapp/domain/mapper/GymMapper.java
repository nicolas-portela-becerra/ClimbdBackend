package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.infrastructure.entity.GymEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymMapper {

    GymDTO toDto(GymEntity entity);

    GymEntity toEntity(GymDTO dto);

    List<GymDTO> toDtoList(List<GymEntity> entities);

    List<GymEntity> toEntityList(List<GymDTO> dtos);
}
