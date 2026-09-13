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

import java.time.LocalDateTime;

@Entity
@Table(name = "wall_image")
@Getter
@Setter
@NoArgsConstructor
public class WallImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "gym_id", nullable = false)
    private Integer gymId;

    @Column(name = "wall_name", nullable = false)
    private String wallName;

    @Column(name = "image_data", nullable = false)
    private byte[] imageData;

    @Column(name = "thumbnail", nullable = false)
    private byte[] thumbnail;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "width_px")
    private Integer widthPx;

    @Column(name = "height_px")
    private Integer heightPx;

    @Column(name = "uploaded_by", nullable = false)
    private Integer uploadedBy;

    @Column(name = "uploaded_date", nullable = false)
    private LocalDateTime uploadedDate;
}
