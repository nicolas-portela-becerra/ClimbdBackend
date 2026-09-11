package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.GymOwnerDTO;
import com.climbingapp.infrastructure.entity.GymOwnerEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymOwnerMapper {

    @Mapping(target = "userId", source = "entity.user.id")
    @Mapping(target = "gymId", source = "entity.gym.id")
    @Mapping(target = "assignedBy", source = "entity.assignedBy.id")
    GymOwnerDTO toDto(GymOwnerEntity entity);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "gym", ignore = true)
    @Mapping(target = "assignedBy", ignore = true)
    GymOwnerEntity toEntity(GymOwnerDTO dto);

    List<GymOwnerDTO> toDtoList(List<GymOwnerEntity> entities);

    List<GymOwnerEntity> toEntityList(List<GymOwnerDTO> dtos);
}
