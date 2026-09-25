package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserJPARepository extends JpaRepository<UserEntity, Integer> {

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByProviderAndProviderUserId(String provider, String providerUserId);

    @Modifying
    @Transactional
    @Query("UPDATE UserEntity u SET u.active = TRUE WHERE u.id IN :userIds")
    void activateUsers(List<Integer> userIds);

    List<UserEntity> findByActiveFalse();
}
