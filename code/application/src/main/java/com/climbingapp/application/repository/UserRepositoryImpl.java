package com.climbingapp.application.repository;

import com.climbingapp.domain.dto.UserCredentials;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.mapper.UserMapper;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.infrastructure.entity.UserEntity;
import com.climbingapp.infrastructure.repository.UserJPARepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    @Autowired private UserJPARepository jpaRepository;

    @Autowired private UserMapper mapper;

    @Override
    public UserDTO findByEmail(String email) {
        UserEntity entity = jpaRepository.findByEmail(email).orElse(null);
        return entity != null ? mapper.toDto(entity) : null;
    }

    @Override
    public UserCredentials findCredentialsByEmail(String email) {
        return jpaRepository.findByEmail(email).map(mapper::toCredentials).orElse(null);
    }

    @Override
    public UserDTO save(UserDTO user) {
        UserEntity entity = mapper.toEntity(user);
        if (entity.getId() != null) {
            jpaRepository
                    .findById(entity.getId())
                    .ifPresent(existing -> entity.setPasswordHash(existing.getPasswordHash()));
        }
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public UserDTO saveWithPassword(UserDTO user, String passwordHash) {
        UserEntity entity = mapper.toEntity(user);
        entity.setPasswordHash(passwordHash);
        return mapper.toDto(jpaRepository.save(entity));
    }

    @Override
    public UserDTO findById(int userId) {
        UserEntity entity = jpaRepository.findById(userId).orElse(null);
        return entity != null ? mapper.toDto(entity) : null;
    }

    @Override
    public void activateUsers(List<Integer> userIds) {
        jpaRepository.activateUsers(userIds);
    }

    @Override
    public List<UserDTO> findDeactivatedUsers() {
        return mapper.toDtoList(jpaRepository.findByActiveFalse());
    }
}
