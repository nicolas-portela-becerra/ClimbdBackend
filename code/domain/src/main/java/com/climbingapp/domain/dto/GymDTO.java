package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GymDTO {

    @EqualsAndHashCode.Exclude private Integer id;

    @EqualsAndHashCode.Exclude private byte[] logo;

    private String name;

    private String location;

    private String description;

    @EqualsAndHashCode.Exclude private Integer creatorId;

    @EqualsAndHashCode.Exclude private Instant createdDate;

    @EqualsAndHashCode.Exclude private Instant updatedDate;

    /** IANA zone id of the gym, e.g. Europe/Madrid. */
    private String timeZone;
}
