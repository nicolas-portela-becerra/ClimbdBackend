package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.GymOwnerDTO;
import com.climbingapp.infrastructure.entity.GymOwnerEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymOwnerMapper {

    GymOwnerDTO toDto(GymOwnerEntity entity);

    GymOwnerEntity toEntity(GymOwnerDTO dto);

    List<GymOwnerDTO> toDtoList(List<GymOwnerEntity> entities);

    List<GymOwnerEntity> toEntityList(List<GymOwnerDTO> dtos);
}
