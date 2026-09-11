package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.infrastructure.entity.GymEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymMapper {

    @Mapping(target = "createdByAdminId", source = "entity.createdByAdmin.id")
    GymDTO toDto(GymEntity entity);

    @Mapping(target = "createdByAdmin", ignore = true)
    GymEntity toEntity(GymDTO dto);

    List<GymDTO> toDtoList(List<GymEntity> entities);

    List<GymEntity> toEntityList(List<GymDTO> dtos);
}
