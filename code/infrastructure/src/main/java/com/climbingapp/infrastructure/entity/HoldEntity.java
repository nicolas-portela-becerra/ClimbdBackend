package com.climbingapp.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "\"hold\"")
@Getter
@Setter
@NoArgsConstructor
public class HoldEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "boulder_id", nullable = false)
    private Integer boulderId;

    @Column(name = "x_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal xRatio;

    @Column(name = "y_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal yRatio;

    @Column(name = "radius_ratio", nullable = false, precision = 5, scale = 4)
    private BigDecimal radiusRatio;

    @Column(name = "sequence_order")
    private Integer sequenceOrder;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
}
