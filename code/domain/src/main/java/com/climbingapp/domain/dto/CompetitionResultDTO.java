package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionResultDTO {

    private Integer id;

    private Integer participantId;

    private Integer boulderId;

    private Integer competitionId;

    private Integer attempts;

    private boolean topped;
}
