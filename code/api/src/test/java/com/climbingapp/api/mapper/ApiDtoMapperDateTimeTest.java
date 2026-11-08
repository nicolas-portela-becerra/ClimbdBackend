package com.climbingapp.api.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.climbingapp.api.dto.CompetitionDto;
import com.climbingapp.api.dto.CompetitionRequest;
import com.climbingapp.domain.dto.CompetitionDTO;

import org.junit.jupiter.api.Test;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class ApiDtoMapperDateTimeTest {

    private final ApiDtoMapper mapper = new ApiDtoMapperImpl();
    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void requestWithAnOffsetBecomesTheSameInstant() {
        CompetitionRequest request =
                jsonMapper.readValue(
                        """
                        {"name":"Open","type":"ALL_OR_NOTHING",
                         "dateFrom":"2026-11-20T19:00:00+01:00",
                         "dateTo":"2026-11-20T13:00:00-05:00"}
                        """,
                        CompetitionRequest.class);

        CompetitionDTO domain = mapper.toDomainCompetition(request);

        assertEquals(Instant.parse("2026-11-20T18:00:00Z"), domain.getDateFrom());
        assertEquals(Instant.parse("2026-11-20T18:00:00Z"), domain.getDateTo());
    }

    @Test
    void requestWithoutAnOffsetIsRejected() {
        assertThrows(
                JacksonException.class,
                () ->
                        jsonMapper.readValue(
                                """
                                {"name":"Open","type":"ALL_OR_NOTHING",
                                 "dateFrom":"2026-11-20T19:00:00",
                                 "dateTo":"2026-11-20T22:00:00"}
                                """,
                                CompetitionRequest.class));
    }

    @Test
    void responsesAreAlwaysUtc() {
        CompetitionDTO domain = new CompetitionDTO();
        domain.setDateFrom(Instant.parse("2026-11-20T18:00:00Z"));
        domain.setDateTo(Instant.parse("2026-11-20T21:00:00Z"));

        CompetitionDto api = mapper.toApiCompetition(domain);

        assertEquals(OffsetDateTime.parse("2026-11-20T18:00:00Z"), api.getDateFrom());
        assertEquals(ZoneOffset.UTC, api.getDateFrom().getOffset());
        assertEquals("\"2026-11-20T18:00:00Z\"", jsonMapper.writeValueAsString(api.getDateFrom()));
    }

    @Test
    void nullsStayNull() {
        assertNull(mapper.toOffset(null));
        assertNull(mapper.toInstant(null));
    }
}
