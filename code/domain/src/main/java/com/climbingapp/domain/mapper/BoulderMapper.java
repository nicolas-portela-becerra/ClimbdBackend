package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.BoulderResponseDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.infrastructure.entity.BoulderEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BoulderMapper {

    BoulderDTO toDto(BoulderEntity entity);

    BoulderEntity toEntity(BoulderDTO dto);

    List<BoulderDTO> toDtoList(List<BoulderEntity> entities);

    List<BoulderEntity> toEntityList(List<BoulderDTO> dtos);

    @Mapping(source = "dto.id", target = "id")
    @Mapping(source = "dto.name", target = "name")
    @Mapping(source = "user.name", target = "creator")
    BoulderResponseDTO toResponseDTO(BoulderDTO dto, UserDTO user);
}
