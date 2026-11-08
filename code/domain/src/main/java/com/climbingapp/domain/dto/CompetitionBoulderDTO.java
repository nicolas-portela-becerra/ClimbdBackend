package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionBoulderDTO {

    private BoulderDTO boulder;

    private List<HoldDTO> holds;
}
