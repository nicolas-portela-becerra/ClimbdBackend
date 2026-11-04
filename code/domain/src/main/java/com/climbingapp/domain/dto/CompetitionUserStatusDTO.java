package com.climbingapp.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** What a participant has stored for a competition. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionUserStatusDTO {

    private CompetitionDTO competition;

    private CompetitionStatus status;

    private CompetitionParticipantDTO participant;

    private List<CompetitionResultDTO> results;

    private int totalPoints;

    /** False when a submitted snapshot was older than the stored one and was ignored. */
    private boolean accepted;
}
