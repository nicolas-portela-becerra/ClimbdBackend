package com.climbingapp.domain.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.CompetitionAnnouncementDTO;
import com.climbingapp.domain.dto.CompetitionBoulderDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.dto.CompetitionParticipantDTO;
import com.climbingapp.domain.dto.CompetitionResultDTO;
import com.climbingapp.domain.dto.CompetitionUserStatusDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.infrastructure.entity.enums.CompetitionSection;

import java.util.List;

public interface CompetitionUseCase {

    CompetitionDTO createCompetition(int gymId, CompetitionDTO competition);

    CompetitionDTO updateCompetition(int competitionId, CompetitionDTO competition);

    List<CompetitionAnnouncementDTO> getActiveCompetitions(int gymId, int userId);

    CompetitionParticipantDTO register(int competitionId, int userId, CompetitionSection section);

    void unregister(int competitionId, int userId);

    List<CompetitionBoulderDTO> getCompetitionBoulders(
            int competitionId, int userId, boolean isAdmin);

    CompetitionUserStatusDTO submitResults(
            int competitionId, int userId, int snapshot, List<CompetitionResultDTO> results);

    CompetitionUserStatusDTO getUserStatus(int competitionId, int userId);

    CompetitionBoulderDTO createCompetitionBoulder(
            int competitionId, BoulderDTO boulder, List<HoldDTO> holds);

    CompetitionBoulderDTO updateCompetitionBoulder(
            int competitionId, BoulderDTO boulder, List<HoldDTO> holds);

    void deleteCompetitionBoulder(int competitionId, int boulderId);
}
