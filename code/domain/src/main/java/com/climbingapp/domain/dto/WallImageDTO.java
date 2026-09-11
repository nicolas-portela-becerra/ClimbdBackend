package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WallImageDTO {

    private Integer id;

    private Integer gymId;

    private String wallName;

    private Boolean isActual;

    private byte[] imageData;

    private byte[] thumbnail;

    private String mimeType;

    private Integer widthPx;

    private Integer heightPx;

    private Integer minDimensionPx;

    private Integer uploadedBy;

    private LocalDateTime uploadedDate;
}
