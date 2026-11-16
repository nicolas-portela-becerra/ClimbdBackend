package com.climbingapp.application.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.CompetitionParticipantRepository;
import com.climbingapp.domain.repository.CompetitionRankingRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.CompetitionResultRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class CompetitionRankingHistoryServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T23:30:00Z");

    @Mock private CompetitionRepository competitionRepository;
    @Mock private CompetitionRankingRepository rankingRepository;
    @Mock private CompetitionResultRepository resultRepository;
    @Mock private CompetitionParticipantRepository participantRepository;
    @Mock private BoulderRepository boulderRepository;

    private final CompetitionRankingHistoryService service = new CompetitionRankingHistoryService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "clock", Clock.fixed(NOW, ZoneOffset.UTC));
        ReflectionTestUtils.setField(service, "competitionRepository", competitionRepository);
        ReflectionTestUtils.setField(service, "competitionRankingRepository", rankingRepository);
        ReflectionTestUtils.setField(service, "competitionResultRepository", resultRepository);
        ReflectionTestUtils.setField(service, "participantRepository", participantRepository);
        ReflectionTestUtils.setField(service, "boulderRepository", boulderRepository);
    }

    @Test
    void historifiesInAnOrderThatRespectsTheForeignKeys() {
        List<Integer> ids = List.of(1, 2);
        when(competitionRepository.findClosedIds(NOW)).thenReturn(ids);

        service.historifyCompetitionJob();

        InOrder order =
                inOrder(
                        rankingRepository,
                        resultRepository,
                        boulderRepository,
                        participantRepository,
                        competitionRepository);
        order.verify(rankingRepository).saveFinalRanking(ids);
        order.verify(resultRepository).deleteByCompetitionIds(ids);
        order.verify(boulderRepository).retireCompetitionBoulders(ids, NOW);
        order.verify(participantRepository).deleteByCompetitionIds(ids);
        order.verify(competitionRepository).deleteByIds(ids);
    }

    @Test
    void nothingToHistorifyOnlyRecordsTheRun() {
        when(competitionRepository.findClosedIds(NOW)).thenReturn(List.of());

        service.historifyCompetitionJob();

        verifyNoInteractions(rankingRepository, resultRepository, boulderRepository);
        verify(competitionRepository, never()).deleteByIds(any());
    }
}
