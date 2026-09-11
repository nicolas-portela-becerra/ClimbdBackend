package com.climbingapp.domain.mapper;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.infrastructure.entity.UserEntity;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO toDto(UserEntity entity);

    UserEntity toEntity(UserDTO dto);

    UserCredentials toCredentials(UserEntity entity);

    List<UserDTO> toDtoList(List<UserEntity> entities);

    List<UserEntity> toEntityList(List<UserDTO> dtos);
}
