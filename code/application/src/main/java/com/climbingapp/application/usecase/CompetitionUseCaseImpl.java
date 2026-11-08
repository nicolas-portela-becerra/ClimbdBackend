package com.climbingapp.application.usecase;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.CompetitionAnnouncementDTO;
import com.climbingapp.domain.dto.CompetitionBoulderDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.dto.CompetitionParticipantDTO;
import com.climbingapp.domain.dto.CompetitionRankingRecord;
import com.climbingapp.domain.dto.CompetitionResultDTO;
import com.climbingapp.domain.dto.CompetitionStatus;
import com.climbingapp.domain.dto.CompetitionUserStatusDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.dto.WallImageDTO;
import com.climbingapp.domain.exception.ConflictException;
import com.climbingapp.domain.exception.ForbiddenException;
import com.climbingapp.domain.exception.NotFoundException;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.CompetitionParticipantRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.CompetitionResultRepository;
import com.climbingapp.domain.repository.GymOwnerRepository;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.domain.repository.HoldRepository;
import com.climbingapp.domain.repository.UserRepository;
import com.climbingapp.domain.repository.WallImageRepository;
import com.climbingapp.domain.usecase.BoulderUseCase;
import com.climbingapp.domain.usecase.CompetitionUseCase;
import com.climbingapp.infrastructure.entity.enums.CompetitionSection;
import com.climbingapp.infrastructure.entity.enums.CompetitionType;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CompetitionUseCaseImpl implements CompetitionUseCase {

    private static final int DEFAULT_GRACE_MINUTES = 5;

    private static final Duration BOULDERS_VISIBLE_AFTER_END = Duration.ofHours(24);

    /** How long an ended competition keeps appearing in the gym announcements. */
    private static final Duration ANNOUNCED_AFTER_END = Duration.ofHours(12);

    @Autowired private Clock clock;

    @Autowired private CompetitionRepository competitionRepository;

    @Autowired private CompetitionParticipantRepository participantRepository;

    @Autowired private CompetitionResultRepository competitionResultRepository;

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private HoldRepository holdRepository;

    @Autowired private GymRepository gymRepository;

    @Autowired private WallImageRepository wallImageRepository;

    @Autowired private UserRepository userRepository;

    @Autowired private GymOwnerRepository gymOwnerRepository;

    @Autowired private CompetitionResultRepository resultRepository;

    @Autowired private BoulderUseCase boulderUseCase;

    @Override
    @Transactional
    public CompetitionDTO createCompetition(int gymId, CompetitionDTO competition) {
        if (gymRepository.findById(gymId) == null) {
            throw new NotFoundException("Gym with id " + gymId + " does not exist.");
        }
        validateCompetition(competition);
        competition.setId(null);
        competition.setGymId(gymId);
        competition.setCreatedDate(Instant.now(clock));
        CompetitionDTO saved = competitionRepository.save(competition);
        log.info("Competition {} created for gym {}", saved.getId(), gymId);
        return saved;
    }

    @Override
    @CacheEvict(cacheNames = BoulderUseCase.COMPETITION_BOULDERS_CACHE, key = "#competitionId")
    @Transactional
    public CompetitionDTO updateCompetition(int competitionId, CompetitionDTO competition) {
        CompetitionDTO current = getNotStartedCompetition(competitionId);
        validateCompetition(competition);

        if (current.getType() != competition.getType()
                && !boulderRepository.findByCompetitionId(competitionId).isEmpty()) {
            throw new ConflictException(
                    "The competition type can't change once it has boulders. Delete them first.");
        }

        competition.setId(current.getId());
        competition.setGymId(current.getGymId());
        competition.setCreatedDate(current.getCreatedDate());
        CompetitionDTO saved = competitionRepository.save(competition);
        log.info("Competition {} updated", competitionId);
        return saved;
    }

    @Override
    public List<CompetitionAnnouncementDTO> getActiveCompetitions(int gymId, int userId) {
        Instant now = Instant.now(clock);
        return competitionRepository
                .findNotEndedByGymId(gymId, now.minus(ANNOUNCED_AFTER_END))
                .stream()
                .map(
                        competition ->
                                new CompetitionAnnouncementDTO(
                                        competition,
                                        statusOf(competition, now),
                                        participantRepository.existsByCompetitionIdAndUserId(
                                                competition.getId(), userId)))
                .toList();
    }

    @Override
    @Transactional
    public CompetitionParticipantDTO register(
            int competitionId, int userId, CompetitionSection section) {
        getNotStartedCompetition(competitionId);
        if (section == null) {
            throw new IllegalArgumentException("A section is required to register.");
        }
        UserDTO user = userRepository.findById(userId);
        if (user == null) {
            throw new NotFoundException("User with id " + userId + " does not exist.");
        }
        if (user.getGender() == null) {
            throw new IllegalArgumentException(
                    "Set your gender in your profile before registering for a competition.");
        }
        if (participantRepository.existsByCompetitionIdAndUserId(competitionId, userId)) {
            throw new ConflictException("You are already registered for this competition.");
        }

        CompetitionParticipantDTO saved = getSaved(competitionId, userId, section, user);
        log.info("User {} registered for competition {}", userId, competitionId);
        return saved;
    }

    private CompetitionParticipantDTO getSaved(
            int competitionId, int userId, CompetitionSection section, UserDTO user) {
        CompetitionParticipantDTO participant = new CompetitionParticipantDTO();
        participant.setCompetitionId(competitionId);
        participant.setUserId(userId);
        participant.setSection(section);
        participant.setGender(user.getGender());
        participant.setLastSnapshot(0);
        participant.setRegisteredAt(Instant.now(clock));
        return participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void unregister(int competitionId, int userId) {
        getNotStartedCompetition(competitionId);
        CompetitionParticipantDTO participant =
                participantRepository.findByCompetitionIdAndUserId(competitionId, userId);
        if (participant == null) {
            throw new NotFoundException("You are not registered for this competition.");
        }
        participantRepository.deleteById(participant.getId());
        log.info("User {} unregistered from competition {}", userId, competitionId);
    }

    @Override
    public List<CompetitionBoulderDTO> getCompetitionBoulders(
            int competitionId, int userId, boolean isAdmin) {
        CompetitionDTO competition = getCompetition(competitionId);
        boolean canManage =
                isAdmin
                        || gymOwnerRepository.existsByGymIdAndUserId(
                                competition.getGymId(), userId);

        if (!canManage) {
            Instant now = Instant.now(clock);
            if (now.isBefore(competition.getDateFrom())) {
                throw new ForbiddenException(
                        "Boulders will be visible when the competition starts.");
            }
            if (now.isAfter(competition.getDateTo().plus(BOULDERS_VISIBLE_AFTER_END))) {
                throw new ForbiddenException(
                        "The boulders of this competition are no longer available.");
            }
        }

        return boulderUseCase.getCompetitionBoulders(competitionId);
    }

    @Override
    @Transactional
    public CompetitionUserStatusDTO submitResults(
            int competitionId, int userId, int snapshot, List<CompetitionResultDTO> results) {
        CompetitionDTO competition = getCompetition(competitionId);
        Instant now = Instant.now(clock);
        if (now.isBefore(competition.getDateFrom())) {
            throw new ConflictException("The competition has not started yet.");
        }
        if (now.isAfter(
                competition.getDateTo().plus(Duration.ofMinutes(competition.getGraceMinutes())))) {
            throw new ConflictException("The competition is closed, results can't be sent.");
        }
        CompetitionParticipantDTO participant =
                participantRepository.findByCompetitionIdAndUserId(competitionId, userId);
        if (participant == null) {
            throw new ForbiddenException("Only registered participants can send results.");
        }
        if (snapshot < 1) {
            throw new IllegalArgumentException("The snapshot number must be 1 or more.");
        }
        validateResults(competitionId, results);

        // Locks the participant row: concurrent sends of the same user are applied one by one
        boolean accepted = participantRepository.advanceSnapshot(participant.getId(), snapshot);
        if (accepted) {
            resultRepository.upsertResults(participant.getId(), competitionId, results);
            log.debug(
                    "Snapshot {} of participant {} stored ({} results)",
                    snapshot,
                    participant.getId(),
                    results.size());
        } else {
            log.debug(
                    "Snapshot {} of participant {} ignored, a newer one is stored",
                    snapshot,
                    participant.getId());
        }
        CompetitionUserStatusDTO status =
                buildUserStatus(competition, participantRepository.findById(participant.getId()));
        status.setAccepted(accepted);
        return status;
    }

    @Override
    public CompetitionUserStatusDTO getUserStatus(int competitionId, int userId) {
        CompetitionDTO competition = getCompetition(competitionId);
        CompetitionParticipantDTO participant =
                participantRepository.findByCompetitionIdAndUserId(competitionId, userId);
        if (participant == null) {
            throw new NotFoundException("You are not registered for this competition.");
        }
        CompetitionUserStatusDTO status = buildUserStatus(competition, participant);
        status.setAccepted(true);
        return status;
    }

    @Override
    @CacheEvict(cacheNames = BoulderUseCase.COMPETITION_BOULDERS_CACHE, key = "#competitionId")
    @Transactional
    public CompetitionBoulderDTO createCompetitionBoulder(
            int competitionId, BoulderDTO boulder, List<HoldDTO> holds) {
        CompetitionDTO competition = getNotStartedCompetition(competitionId);
        validateCompetitionBoulder(competition, boulder, holds);

        Instant now = Instant.now(clock);
        boulder.setId(null);
        boulder.setGymId(competition.getGymId());
        boulder.setCompetitionId(competitionId);
        boulder.setCreatedDate(now);
        BoulderDTO saved = boulderRepository.save(boulder);
        List<HoldDTO> savedHolds = saveHolds(saved.getId(), holds, now);

        log.info("Boulder {} created for competition {}", saved.getId(), competitionId);
        return new CompetitionBoulderDTO(saved, savedHolds);
    }

    @Override
    @CacheEvict(cacheNames = BoulderUseCase.COMPETITION_BOULDERS_CACHE, key = "#competitionId")
    @Transactional
    public CompetitionBoulderDTO updateCompetitionBoulder(
            int competitionId, BoulderDTO boulder, List<HoldDTO> holds) {
        CompetitionDTO competition = getNotStartedCompetition(competitionId);
        BoulderDTO current = getCompetitionBoulder(competitionId, boulder.getId());
        validateCompetitionBoulder(competition, boulder, holds);

        Instant now = Instant.now(clock);
        current.setWallImageId(boulder.getWallImageId());
        current.setName(boulder.getName());
        current.setGrade(boulder.getGrade());
        current.setDescription(boulder.getDescription());
        current.setPoints(boulder.getPoints());
        current.setUpdatedDate(now);
        BoulderDTO saved = boulderRepository.save(current);

        holdRepository.deleteByBoulderId(saved.getId());
        List<HoldDTO> savedHolds = saveHolds(saved.getId(), holds, now);

        log.info("Boulder {} of competition {} updated", saved.getId(), competitionId);
        return new CompetitionBoulderDTO(saved, savedHolds);
    }

    @Override
    @CacheEvict(cacheNames = BoulderUseCase.COMPETITION_BOULDERS_CACHE, key = "#competitionId")
    @Transactional
    public void deleteCompetitionBoulder(int competitionId, int boulderId) {
        getNotStartedCompetition(competitionId);
        getCompetitionBoulder(competitionId, boulderId);
        holdRepository.deleteByBoulderId(boulderId);
        boulderRepository.deleteById(boulderId);
        log.info("Boulder {} of competition {} deleted", boulderId, competitionId);
    }

    @Override
    public List<CompetitionRankingRecord> getCompetitionRanking(Integer competitionId) {
        getCompetition(competitionId);
        Instant now = Instant.now(clock);
        return competitionResultRepository.getCompetitionRanking(competitionId, Timestamp.from(now));
    }

    private void validateResults(int competitionId, List<CompetitionResultDTO> results) {
        if (results == null) {
            throw new IllegalArgumentException("Results are required.");
        }
        Set<Integer> competitionBoulderIds =
                boulderUseCase.getCompetitionBoulders(competitionId).stream()
                        .map(boulder -> boulder.getBoulder().getId())
                        .collect(Collectors.toSet());
        Set<Integer> seen = new HashSet<>();
        for (CompetitionResultDTO result : results) {
            if (result.getBoulderId() == null
                    || !competitionBoulderIds.contains(result.getBoulderId())) {
                throw new IllegalArgumentException(
                        "Boulder " + result.getBoulderId() + " is not part of this competition.");
            }
            if (!seen.add(result.getBoulderId())) {
                throw new IllegalArgumentException(
                        "Boulder " + result.getBoulderId() + " appears more than once.");
            }
            if (result.getAttempts() == null || result.getAttempts() < 0) {
                throw new IllegalArgumentException("Attempts can't be negative.");
            }
            if (result.isTopped() && result.getAttempts() < 1) {
                throw new IllegalArgumentException("A topped boulder needs at least 1 attempt.");
            }
        }
    }

    private CompetitionUserStatusDTO buildUserStatus(
            CompetitionDTO competition, CompetitionParticipantDTO participant) {
        List<CompetitionResultDTO> results =
                resultRepository.findByParticipantId(participant.getId());
        int totalPoints =
                results.stream()
                        .mapToInt(result -> result.getPoints() == null ? 0 : result.getPoints())
                        .sum();
        CompetitionUserStatusDTO status = new CompetitionUserStatusDTO();
        status.setCompetition(competition);
        status.setStatus(statusOf(competition, Instant.now(clock)));
        status.setParticipant(participant);
        status.setResults(results);
        status.setTotalPoints(totalPoints);
        return status;
    }

    private static CompetitionStatus statusOf(CompetitionDTO competition, Instant now) {
        if (now.isBefore(competition.getDateFrom())) {
            return CompetitionStatus.UPCOMING;
        }
        return now.isBefore(competition.getDateTo())
                ? CompetitionStatus.RUNNING
                : CompetitionStatus.ENDED;
    }

    private List<HoldDTO> saveHolds(Integer boulderId, List<HoldDTO> holds, Instant now) {
        holds.forEach(
                hold -> {
                    hold.setId(null);
                    hold.setBoulderId(boulderId);
                    hold.setCreatedDate(now);
                    hold.setUpdatedDate(now);
                });
        return holdRepository.saveAll(holds);
    }

    private CompetitionDTO getCompetition(int competitionId) {
        CompetitionDTO competition = competitionRepository.findById(competitionId);
        if (competition == null) {
            throw new NotFoundException(
                    "Competition with id " + competitionId + " does not exist.");
        }
        return competition;
    }

    private CompetitionDTO getNotStartedCompetition(int competitionId) {
        CompetitionDTO competition = getCompetition(competitionId);
        if (!Instant.now(clock).isBefore(competition.getDateFrom())) {
            throw new ConflictException("The competition has already started.");
        }
        return competition;
    }

    private BoulderDTO getCompetitionBoulder(int competitionId, Integer boulderId) {
        BoulderDTO boulder = boulderId == null ? null : boulderRepository.findById(boulderId);
        if (boulder == null || !Integer.valueOf(competitionId).equals(boulder.getCompetitionId())) {
            throw new NotFoundException(
                    "Boulder " + boulderId + " does not belong to competition " + competitionId);
        }
        return boulder;
    }

    private void validateCompetition(CompetitionDTO competition) {
        if (!StringUtils.hasText(competition.getName())) {
            throw new IllegalArgumentException("The competition name is required.");
        }
        if (competition.getType() == null) {
            throw new IllegalArgumentException("The competition type is required.");
        }
        if (competition.getDateFrom() == null || competition.getDateTo() == null) {
            throw new IllegalArgumentException("Start and end dates are required.");
        }
        if (!competition.getDateTo().isAfter(competition.getDateFrom())) {
            throw new IllegalArgumentException("The end date must be after the start date.");
        }
        if (!competition.getDateFrom().isAfter(Instant.now(clock))) {
            throw new IllegalArgumentException("The start date must be in the future.");
        }
        if (competition.getGraceMinutes() == null) {
            competition.setGraceMinutes(DEFAULT_GRACE_MINUTES);
        } else if (competition.getGraceMinutes() < 0) {
            throw new IllegalArgumentException("The grace period can't be negative.");
        }

        Integer base = competition.getBasePoints();
        Integer penalty = competition.getAttemptPenalty();
        Integer min = competition.getMinPoints();
        if (competition.getType() == CompetitionType.X_ATTEMPTS) {
            if (base == null || penalty == null || min == null) {
                throw new IllegalArgumentException(
                        "X_ATTEMPTS competitions require basePoints, attemptPenalty and minPoints.");
            }
            if (base < 0 || penalty < 0 || min < 0 || min > base) {
                throw new IllegalArgumentException(
                        "Scoring values must be positive and minPoints can't exceed basePoints.");
            }
        } else if (base != null || penalty != null || min != null) {
            throw new IllegalArgumentException(
                    "ALL_OR_NOTHING competitions use points per boulder, not basePoints,"
                            + " attemptPenalty or minPoints.");
        }
    }

    private void validateCompetitionBoulder(
            CompetitionDTO competition, BoulderDTO boulder, List<HoldDTO> holds) {
        if (!StringUtils.hasText(boulder.getName()) || !StringUtils.hasText(boulder.getGrade())) {
            throw new IllegalArgumentException("Boulder name and grade are required.");
        }
        if (holds == null || holds.isEmpty()) {
            throw new IllegalArgumentException("A boulder needs at least one hold.");
        }
        WallImageDTO wallImage =
                boulder.getWallImageId() == null
                        ? null
                        : wallImageRepository.findById(boulder.getWallImageId());
        if (wallImage == null || !competition.getGymId().equals(wallImage.getGymId())) {
            throw new IllegalArgumentException(
                    "wallImageId does not reference a wall image of gym " + competition.getGymId());
        }
        if (competition.getType() == CompetitionType.ALL_OR_NOTHING) {
            if (boulder.getPoints() == null || boulder.getPoints() < 0) {
                throw new IllegalArgumentException(
                        "ALL_OR_NOTHING boulders require a points value of 0 or more.");
            }
        } else if (boulder.getPoints() != null) {
            throw new IllegalArgumentException(
                    "X_ATTEMPTS boulders use the competition scoring, points must be empty.");
        }
    }
}
