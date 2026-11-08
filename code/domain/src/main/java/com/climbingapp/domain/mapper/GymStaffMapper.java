package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.GymStaffDTO;
import com.climbingapp.infrastructure.entity.GymStaffEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface GymStaffMapper {

    GymStaffDTO toDto(GymStaffEntity entity);

    GymStaffEntity toEntity(GymStaffDTO dto);

    List<GymStaffDTO> toDtoList(List<GymStaffEntity> entities);

    List<GymStaffEntity> toEntityList(List<GymStaffDTO> dtos);
}
