package com.climbingapp.domain.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoulderResponseDTO {

    private int id;

    private int gymId;

    private int wallImageId;

    private String creator;

    private String name;

    private String grade;

    private String description;
}
