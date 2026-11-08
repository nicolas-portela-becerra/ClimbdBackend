package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionAnnouncementDTO {

    private CompetitionDTO competition;

    private CompetitionStatus status;

    private boolean registered;
}
