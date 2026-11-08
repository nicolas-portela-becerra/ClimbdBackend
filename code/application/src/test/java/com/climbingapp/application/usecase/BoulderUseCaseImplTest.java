package com.climbingapp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.repository.BoulderRepository;
import com.climbingapp.domain.repository.HoldRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class BoulderUseCaseImplTest {

    @Mock private BoulderRepository boulderRepository;
    @Mock private HoldRepository holdRepository;

    @Test
    void updateBoulderPinsHoldsToTheUpdatedBoulder() {
        BoulderUseCaseImpl useCase = new BoulderUseCaseImpl();
        ReflectionTestUtils.setField(useCase, "boulderRepository", boulderRepository);
        ReflectionTestUtils.setField(useCase, "holdRepository", holdRepository);
        ReflectionTestUtils.setField(
                useCase, "clock", Clock.fixed(Instant.EPOCH, ZoneOffset.UTC));

        BoulderDTO own = new BoulderDTO();
        own.setId(1);
        own.setName("b");
        HoldDTO existing = new HoldDTO();
        existing.setBoulderId(1);
        when(boulderRepository.findById(1)).thenReturn(own);
        when(holdRepository.findByBoulderIdOrderBySequenceOrder(1)).thenReturn(List.of(existing));

        // Attacker names a victim boulder (99) and an existing hold id in the body.
        HoldDTO injected1 = new HoldDTO();
        injected1.setId(500);
        injected1.setBoulderId(99);
        HoldDTO injected2 = new HoldDTO();
        injected2.setBoulderId(99);
        BoulderDTO request = new BoulderDTO();
        request.setId(1);
        request.setName("b");

        useCase.updateBoulder(request, List.of(injected1, injected2));

        verify(holdRepository).deleteByBoulderId(1);
        verify(holdRepository).saveAll(List.of(injected1, injected2));
        for (HoldDTO hold : List.of(injected1, injected2)) {
            assertEquals(1, hold.getBoulderId());
            assertNull(hold.getId());
        }
    }
}
