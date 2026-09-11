package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserJPARepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByProviderAndProviderUserId(String provider, String providerUserId);
}
