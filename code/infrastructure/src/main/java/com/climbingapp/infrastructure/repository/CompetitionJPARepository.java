package com.climbingapp.infrastructure.repository;

import com.climbingapp.infrastructure.entity.CompetitionEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface CompetitionJPARepository extends JpaRepository<CompetitionEntity, Integer> {

    Page<CompetitionEntity> findByGymId(Integer gymId, Pageable pageable);

    boolean existsByIdAndGymId(Integer id, Integer gymId);

    @Query(
            """
        SELECT c FROM CompetitionEntity c
        WHERE c.gymId = :gymId
        AND c.dateTo > :threshold
        ORDER BY c.dateFrom ASC
    """)
    List<CompetitionEntity> findByGymIdAndDateToAfterOrderByDateFromAsc(
            Integer gymId, Instant threshold);
}
