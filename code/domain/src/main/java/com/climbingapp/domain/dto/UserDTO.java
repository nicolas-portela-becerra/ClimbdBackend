package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Integer id;

    private String email;

    private String name;

    private String role;

    private String provider;

    private String providerUserId;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;
}
