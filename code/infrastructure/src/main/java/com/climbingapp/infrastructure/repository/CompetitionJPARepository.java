package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface CompetitionJPARepository extends JpaRepository<CompetitionEntity, Integer> {

    Page<CompetitionEntity> findByGymId(Integer gymId, Pageable pageable);

    boolean existsByIdAndGymId(Integer id, Integer gymId);

    List<CompetitionEntity> findByGymIdAndDateToAfterOrderByDateFromAsc(
            Integer gymId, LocalDateTime now);
}
