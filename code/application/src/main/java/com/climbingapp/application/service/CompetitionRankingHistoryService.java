package com.climbingapp.application.service;

import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.CompetitionParticipantRepository;
import com.climbingapp.domain.repository.CompetitionRankingRepository;
import com.climbingapp.domain.repository.CompetitionRepository;
import com.climbingapp.domain.repository.CompetitionResultRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class CompetitionRankingHistoryService {

    @Autowired private Clock clock;

    @Autowired private BoulderRepository boulderRepository;

    @Autowired private CompetitionRepository competitionRepository;

    @Autowired private CompetitionResultRepository competitionResultRepository;

    @Autowired private CompetitionParticipantRepository participantRepository;

    @Autowired private CompetitionRankingRepository competitionRankingRepository;

    /**
     * Stores the final ranking of every closed competition, turns its boulders into regular ones
     * and deletes the competition. Picking "every closed competition still in the table" instead of
     * a date window means a night the app was down is caught up by the next run. Everything is one
     * transaction, so a failure leaves the data untouched and the next run retries.
     */
    @Scheduled(cron = "0 30 23 * * *")
    @Transactional
    public void historifyCompetitionJob() {
        Instant now = Instant.now(clock);
        List<Integer> ids = competitionRepository.findClosedIds(now);
        if (!ids.isEmpty()) {
            competitionRankingRepository.saveFinalRanking(ids);
            competitionResultRepository.deleteByCompetitionIds(ids);
            boulderRepository.retireCompetitionBoulders(ids, now);
            participantRepository.deleteByCompetitionIds(ids);
            competitionRepository.deleteByIds(ids);
        }
        log.info("Historified {} competitions: {}", ids.size(), ids);
    }
}
