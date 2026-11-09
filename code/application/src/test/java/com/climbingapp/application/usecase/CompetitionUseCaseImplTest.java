package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.CompetitionAnnouncementDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.dto.CompetitionStatus;
import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.exception.ConflictException;
import com.climbingapp.domain.exception.ForbiddenException;
import com.climbingapp.domain.repository.CompetitionParticipantRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.GymRepository;
import com.climbingapp.domain.repository.GymStaffRepository;
import com.climbingapp.domain.usecase.BoulderUseCase;
import com.climbingapp.infrastructure.entity.enums.CompetitionSection;
import com.climbingapp.infrastructure.entity.enums.CompetitionType;
import com.climbingapp.infrastructure.entity.enums.GymRole;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompetitionUseCaseImplTest {

    private static final Instant NOW = Instant.parse("2026-11-20T18:00:00Z");
    private static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    @Mock private CompetitionRepository competitionRepository;
    @Mock private CompetitionParticipantRepository participantRepository;
    @Mock private GymStaffRepository gymStaffRepository;
    @Mock private GymRepository gymRepository;
    @Mock private BoulderUseCase boulderUseCase;

    private final CompetitionUseCaseImpl useCase = new CompetitionUseCaseImpl();

    @BeforeEach
    void setUp() {
        useAt(NOW);
        ReflectionTestUtils.setField(useCase, "competitionRepository", competitionRepository);
        ReflectionTestUtils.setField(useCase, "participantRepository", participantRepository);
        ReflectionTestUtils.setField(useCase, "gymStaffRepository", gymStaffRepository);
        ReflectionTestUtils.setField(useCase, "gymRepository", gymRepository);
        ReflectionTestUtils.setField(useCase, "boulderUseCase", boulderUseCase);
    }

    private void useAt(Instant instant) {
        ReflectionTestUtils.setField(useCase, "clock", Clock.fixed(instant, ZoneOffset.UTC));
    }

    private static CompetitionDTO competition(Instant from, Instant to, int graceMinutes) {
        CompetitionDTO competition = new CompetitionDTO();
        competition.setId(1);
        competition.setGymId(10);
        competition.setName("Open");
        competition.setType(CompetitionType.ALL_OR_NOTHING);
        competition.setDateFrom(from);
        competition.setDateTo(to);
        competition.setGraceMinutes(graceMinutes);
        return competition;
    }

    private CompetitionStatus statusAt(CompetitionDTO competition, Instant now) {
        useAt(now);
        when(competitionRepository.findNotEndedByGymId(any(), any()))
                .thenReturn(List.of(competition));
        when(participantRepository.existsByCompetitionIdAndUserId(1, 5)).thenReturn(false);
        List<CompetitionAnnouncementDTO> announcements = useCase.getActiveCompetitions(10, 5);
        return announcements.getFirst().getStatus();
    }

    // ---- status boundaries ------------------------------------------------------------

    @Test
    void statusIsUpcomingRunningOrEndedWithStartInclusiveAndEndExclusive() {
        Instant from = NOW;
        Instant to = NOW.plus(Duration.ofHours(2));
        CompetitionDTO competition = competition(from, to, 5);

        assertEquals(CompetitionStatus.UPCOMING, statusAt(competition, from.minusSeconds(1)));
        assertEquals(CompetitionStatus.RUNNING, statusAt(competition, from));
        assertEquals(CompetitionStatus.RUNNING, statusAt(competition, to.minusSeconds(1)));
        assertEquals(CompetitionStatus.ENDED, statusAt(competition, to));
    }

    @Test
    void competitionAcrossMadridDstChangeLastsItsRealDuration() {
        // 2026-10-25: Madrid goes back from CEST (UTC+2) to CET (UTC+1) at 03:00 -> 02:00.
        Instant from =
                ZonedDateTime.of(LocalDateTime.parse("2026-10-25T01:00:00"), MADRID).toInstant();
        Instant to =
                ZonedDateTime.of(LocalDateTime.parse("2026-10-25T04:00:00"), MADRID).toInstant();
        assertEquals(Duration.ofHours(4), Duration.between(from, to)); // not 3 h of wall clock

        CompetitionDTO competition = competition(from, to, 5);
        assertEquals(CompetitionStatus.RUNNING, statusAt(competition, to.minusSeconds(1)));
        assertEquals(CompetitionStatus.ENDED, statusAt(competition, to));
    }

    @Test
    void activeCompetitionsAreQueriedWithTheTwelveHourAnnouncementThreshold() {
        when(competitionRepository.findNotEndedByGymId(any(), any())).thenReturn(List.of());

        useCase.getActiveCompetitions(10, 5);

        ArgumentCaptor<Instant> threshold = ArgumentCaptor.forClass(Instant.class);
        verify(competitionRepository).findNotEndedByGymId(any(), threshold.capture());
        assertEquals(NOW.minus(Duration.ofHours(12)), threshold.getValue());
    }

    // ---- result window ----------------------------------------------------------------

    @Test
    void submitResultsIsRejectedBeforeTheCompetitionStarts() {
        when(competitionRepository.findById(1))
                .thenReturn(competition(NOW.plusSeconds(1), NOW.plus(Duration.ofHours(2)), 5));

        assertThrows(ConflictException.class, () -> useCase.submitResults(1, 5, 1, List.of()));
    }

    @Test
    void submitResultsWindowIncludesTheGracePeriodAndClosesAfterIt() {
        Instant to = NOW.minus(Duration.ofMinutes(5));
        when(competitionRepository.findById(1))
                .thenReturn(competition(NOW.minus(Duration.ofHours(2)), to, 5));
        when(participantRepository.findByCompetitionIdAndUserId(1, 5)).thenReturn(null);

        // now == dateTo + grace: still open, so it gets past the window and fails on the
        // participant
        assertThrows(ForbiddenException.class, () -> useCase.submitResults(1, 5, 1, List.of()));

        useAt(NOW.plusSeconds(1)); // one second after the grace period
        assertThrows(ConflictException.class, () -> useCase.submitResults(1, 5, 1, List.of()));
    }

    // ---- registration lock --------------------------------------------------------------

    @Test
    void registerIsRejectedOnceTheCompetitionHasStarted() {
        when(competitionRepository.findById(1))
                .thenReturn(competition(NOW, NOW.plus(Duration.ofHours(2)), 5));

        assertThrows(
                ConflictException.class, () -> useCase.register(1, 5, CompetitionSection.OPEN));
    }

    // ---- boulder visibility -------------------------------------------------------------

    @Test
    void bouldersAreHiddenBeforeStartAndTwentyFourHoursAfterTheEnd() {
        Instant from = NOW.plus(Duration.ofHours(1));
        Instant to = from.plus(Duration.ofHours(2));
        when(competitionRepository.findById(1)).thenReturn(competition(from, to, 5));
        when(gymStaffRepository.findRole(10, 5)).thenReturn(null);

        assertThrows(ForbiddenException.class, () -> useCase.getCompetitionBoulders(1, 5, false));

        useAt(to.plus(Duration.ofHours(24))); // last visible instant
        assertDoesNotThrow(() -> useCase.getCompetitionBoulders(1, 5, false));

        useAt(to.plus(Duration.ofHours(24)).plusSeconds(1));
        assertThrows(ForbiddenException.class, () -> useCase.getCompetitionBoulders(1, 5, false));
    }

    @Test
    void settersSeeTheBouldersBeforeTheStart() {
        Instant from = NOW.plus(Duration.ofHours(1));
        when(competitionRepository.findById(1))
                .thenReturn(competition(from, from.plus(Duration.ofHours(2)), 5));
        when(gymStaffRepository.findRole(10, 5)).thenReturn(GymRole.SETTER);

        assertDoesNotThrow(() -> useCase.getCompetitionBoulders(1, 5, false));
    }

    // ---- creation -----------------------------------------------------------------------

    @Test
    void createCompetitionStampsCreatedDateFromTheClockAndKeepsTheInstants() {
        Instant from = ZonedDateTime.parse("2026-11-27T19:00:00+01:00").toInstant();
        Instant to = ZonedDateTime.parse("2026-11-27T22:00:00+01:00").toInstant();
        when(gymRepository.findById(10)).thenReturn(new GymDTO());
        when(competitionRepository.save(any(CompetitionDTO.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CompetitionDTO saved = useCase.createCompetition(10, competition(from, to, 5));

        assertEquals(NOW, saved.getCreatedDate());
        assertEquals(Instant.parse("2026-11-27T18:00:00Z"), saved.getDateFrom());
    }

    @Test
    void createCompetitionRejectsAStartThatIsNotInTheFuture() {
        when(gymRepository.findById(10)).thenReturn(new GymDTO());

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        useCase.createCompetition(
                                10, competition(NOW, NOW.plus(Duration.ofHours(2)), 5)));
    }
}
