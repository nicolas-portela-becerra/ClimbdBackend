package com.climbingapp.api.controller;

import com.climbingapp.api.dto.CompetitionAnnouncementDto;
import com.climbingapp.api.dto.CompetitionBoulderDto;
import com.climbingapp.api.dto.CompetitionBoulderRequest;
import com.climbingapp.api.dto.CompetitionDto;
import com.climbingapp.api.dto.CompetitionParticipantDto;
import com.climbingapp.api.dto.CompetitionRankingEntryDto;
import com.climbingapp.api.dto.CompetitionRegistrationRequest;
import com.climbingapp.api.dto.CompetitionRequest;
import com.climbingapp.api.dto.CompetitionUserStatusDto;
import com.climbingapp.api.dto.CreateHoldRequest;
import com.climbingapp.api.dto.SubmitCompetitionResultsRequest;
import com.climbingapp.api.mapper.ApiDtoMapper;
import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.usecase.CompetitionUseCase;
import com.climbingapp.infrastructure.entity.enums.CompetitionSection;
import com.climbingapp.jwt.utils.AuthenticatedUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CompetitionController implements CompetitionsApi {

    @Autowired private CompetitionUseCase competitionUseCase;

    @Autowired private ApiDtoMapper mapper;

    @Override
    @PreAuthorize("@gymSecurity.isStaff(#gymId, authentication)")
    public ResponseEntity<CompetitionDto> createCompetition(
            @PathVariable("gymId") Integer gymId, CompetitionRequest competitionRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        mapper.toApiCompetition(
                                competitionUseCase.createCompetition(
                                        gymId, mapper.toDomainCompetition(competitionRequest))));
    }

    @Override
    @PreAuthorize("@gymSecurity.isCompetitionStaff(#competitionId, authentication)")
    public ResponseEntity<CompetitionDto> updateCompetition(
            @PathVariable("competitionId") Integer competitionId,
            CompetitionRequest competitionRequest) {
        return ResponseEntity.ok(
                mapper.toApiCompetition(
                        competitionUseCase.updateCompetition(
                                competitionId, mapper.toDomainCompetition(competitionRequest))));
    }

    @Override
    public ResponseEntity<List<CompetitionAnnouncementDto>> listActiveCompetitions(
            @PathVariable("gymId") Integer gymId) {
        return ResponseEntity.ok(
                mapper.toApiAnnouncements(
                        competitionUseCase.getActiveCompetitions(gymId, currentUserId())));
    }

    @Override
    public ResponseEntity<CompetitionParticipantDto> registerForCompetition(
            @PathVariable("competitionId") Integer competitionId,
            CompetitionRegistrationRequest competitionRegistrationRequest) {
        CompetitionSection section =
                CompetitionSection.valueOf(competitionRegistrationRequest.getSection().name());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        mapper.toApiParticipant(
                                competitionUseCase.register(
                                        competitionId, currentUserId(), section)));
    }

    @Override
    public ResponseEntity<Void> unregisterFromCompetition(
            @PathVariable("competitionId") Integer competitionId) {
        competitionUseCase.unregister(competitionId, currentUserId());
        return ResponseEntity.noContent().build();
    }

    @Override
    @PreAuthorize("@gymSecurity.isCompetitionStaff(#competitionId, authentication)")
    public ResponseEntity<List<CompetitionRankingEntryDto>> getCompetitionRanking(
            @PathVariable("competitionId") Integer competitionId) {
        return ResponseEntity.ok(
                mapper.toApiRanking(competitionUseCase.getCompetitionRanking(competitionId)));
    }

    @Override
    public ResponseEntity<List<CompetitionBoulderDto>> listCompetitionBoulders(
            @PathVariable("competitionId") Integer competitionId) {
        return ResponseEntity.ok(
                mapper.toApiCompetitionBoulders(
                        competitionUseCase.getCompetitionBoulders(
                                competitionId,
                                currentUserId(),
                                AuthenticatedUser.current().isAdmin())));
    }

    @Override
    public ResponseEntity<CompetitionUserStatusDto> submitCompetitionResults(
            @PathVariable("competitionId") Integer competitionId,
            SubmitCompetitionResultsRequest request) {
        return ResponseEntity.ok(
                mapper.toApiUserStatus(
                        competitionUseCase.submitResults(
                                competitionId,
                                currentUserId(),
                                request.getSnapshot(),
                                mapper.toDomainResults(request.getResults()))));
    }

    @Override
    public ResponseEntity<CompetitionUserStatusDto> getMyCompetitionStatus(
            @PathVariable("competitionId") Integer competitionId) {
        return ResponseEntity.ok(
                mapper.toApiUserStatus(
                        competitionUseCase.getUserStatus(competitionId, currentUserId())));
    }

    @Override
    @PreAuthorize("@gymSecurity.isCompetitionStaff(#competitionId, authentication)")
    public ResponseEntity<CompetitionBoulderDto> createCompetitionBoulder(
            @PathVariable("competitionId") Integer competitionId,
            CompetitionBoulderRequest competitionBoulderRequest) {
        BoulderDTO boulder = mapper.toDomainCompetitionBoulder(competitionBoulderRequest);
        boulder.setCreatorId(currentUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        mapper.toApiCompetitionBoulder(
                                competitionUseCase.createCompetitionBoulder(
                                        competitionId,
                                        boulder,
                                        toDomainHolds(competitionBoulderRequest.getHolds()))));
    }

    @Override
    @PreAuthorize("@gymSecurity.isCompetitionStaff(#competitionId, authentication)")
    public ResponseEntity<CompetitionBoulderDto> updateCompetitionBoulder(
            @PathVariable("competitionId") Integer competitionId,
            @PathVariable("boulderId") Integer boulderId,
            CompetitionBoulderRequest competitionBoulderRequest) {
        BoulderDTO boulder = mapper.toDomainCompetitionBoulder(competitionBoulderRequest);
        boulder.setId(boulderId);
        return ResponseEntity.ok(
                mapper.toApiCompetitionBoulder(
                        competitionUseCase.updateCompetitionBoulder(
                                competitionId,
                                boulder,
                                toDomainHolds(competitionBoulderRequest.getHolds()))));
    }

    @Override
    @PreAuthorize("@gymSecurity.isCompetitionStaff(#competitionId, authentication)")
    public ResponseEntity<Void> deleteCompetitionBoulder(
            @PathVariable("competitionId") Integer competitionId,
            @PathVariable("boulderId") Integer boulderId) {
        competitionUseCase.deleteCompetitionBoulder(competitionId, boulderId);
        return ResponseEntity.noContent().build();
    }

    private List<HoldDTO> toDomainHolds(List<CreateHoldRequest> requestHolds) {
        List<CreateHoldRequest> source = requestHolds != null ? requestHolds : List.of();
        List<HoldDTO> holds = new ArrayList<>(source.size());
        for (int i = 0; i < source.size(); i++) {
            HoldDTO hold = mapper.toDomainHold(source.get(i));
            if (hold.getColor() == null) {
                hold.setColor(i);
            }
            holds.add(hold);
        }
        return holds;
    }

    private static Integer currentUserId() {
        return AuthenticatedUser.current().getId();
    }
}
