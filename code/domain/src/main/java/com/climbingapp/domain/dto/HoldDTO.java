package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoldDTO {

    private Integer id;

    private Integer boulderId;

    private BigDecimal xRatio;

    private BigDecimal yRatio;

    private BigDecimal radiusRatio;

    private Integer sequenceOrder;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
