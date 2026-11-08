package com.climbingapp.api.mapper;

import com.climbingapp.api.dto.BoulderDetailDto;
import com.climbingapp.api.dto.BoulderDto;
import com.climbingapp.api.dto.BoulderPageResponse;
import com.climbingapp.api.dto.BoulderResultRequest;
import com.climbingapp.api.dto.BoulderWithUserPageResponse;
import com.climbingapp.api.dto.CompetitionAnnouncementDto;
import com.climbingapp.api.dto.CompetitionBoulderDto;
import com.climbingapp.api.dto.CompetitionBoulderRequest;
import com.climbingapp.api.dto.CompetitionDto;
import com.climbingapp.api.dto.CompetitionParticipantDto;
import com.climbingapp.api.dto.CompetitionRankingEntryDto;
import com.climbingapp.api.dto.CompetitionRequest;
import com.climbingapp.api.dto.CompetitionUserStatusDto;
import com.climbingapp.api.dto.CreateHoldRequest;
import com.climbingapp.api.dto.GymDto;
import com.climbingapp.api.dto.GymPageResponse;
import com.climbingapp.api.dto.GymStaffDto;
import com.climbingapp.api.dto.HoldDto;
import com.climbingapp.api.dto.UserDto;
import com.climbingapp.api.dto.UserGymRoleDto;
import com.climbingapp.api.dto.WallImageDetail;
import com.climbingapp.api.dto.WallImagePageResponse;
import com.climbingapp.api.dto.WallImageSummary;
import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.BoulderResponseDTO;
import com.climbingapp.domain.dto.CompetitionAnnouncementDTO;
import com.climbingapp.domain.dto.CompetitionBoulderDTO;
import com.climbingapp.domain.dto.CompetitionDTO;
import com.climbingapp.domain.dto.CompetitionParticipantDTO;
import com.climbingapp.domain.dto.CompetitionRankingRecord;
import com.climbingapp.domain.dto.CompetitionResultDTO;
import com.climbingapp.domain.dto.CompetitionUserStatusDTO;
import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.GymStaffMemberDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.dto.UserDTO;
import com.climbingapp.domain.dto.UserGymRoleDTO;
import com.climbingapp.domain.dto.WallImageDTO;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiDtoMapper {

    UserDto toUserDto(UserDTO userDto);

    List<UserDto> toUserDtos(List<UserDTO> userDtos);

    @Mapping(target = "createdByAdminId", source = "creatorId")
    GymDto toApiGym(GymDTO dto);

    GymPageResponse toGymPage(Page<GymDTO> page);

    GymStaffDto toApiStaff(GymStaffMemberDTO dto);

    List<GymStaffDto> toApiStaff(List<GymStaffMemberDTO> dtos);

    List<UserGymRoleDto> toApiGymRoles(List<UserGymRoleDTO> dtos);

    BoulderDto toApiBoulder(BoulderDTO dto);

    BoulderPageResponse toBoulderPage(Page<BoulderDTO> page);

    BoulderWithUserPageResponse toBoulderWithUserPage(Page<BoulderResponseDTO> page);

    @Mapping(target = "id", source = "boulder.id")
    @Mapping(target = "holds", source = "holds")
    @Mapping(target = "creator", source = "creator.name")
    @Mapping(target = "name", source = "boulder.name")
    BoulderDetailDto toBoulderDetail(BoulderDTO boulder, List<HoldDTO> holds, UserDTO creator);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boulderId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    HoldDTO toDomainHold(CreateHoldRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Named("toDomainHoldDTO")
    HoldDTO toDomainHoldDTO(HoldDto holdDto);

    @IterableMapping(qualifiedByName = "toDomainHoldDTO")
    List<HoldDTO> toDomainHoldDTOs(List<HoldDto> holds);

    HoldDto toApiHold(HoldDTO dto);

    WallImagePageResponse toWallImagePage(Page<WallImageDTO> page);

    @Mapping(
            target = "thumbnailUrl",
            expression = "java(\"/api/wall-images/\" + dto.getId() + \"/thumbnail\")")
    WallImageSummary toWallImageSummary(WallImageDTO dto);

    @Mapping(
            target = "imageUrl",
            expression = "java(\"/api/wall-images/\" + dto.getId() + \"/data\")")
    @Mapping(
            target = "thumbnailUrl",
            expression = "java(\"/api/wall-images/\" + dto.getId() + \"/thumbnail\")")
    WallImageDetail toWallImageDetail(WallImageDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gymId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    CompetitionDTO toDomainCompetition(CompetitionRequest request);

    CompetitionDto toApiCompetition(CompetitionDTO dto);

    @Mapping(target = "id", source = "competition.id")
    @Mapping(target = "name", source = "competition.name")
    @Mapping(target = "type", source = "competition.type")
    @Mapping(target = "dateFrom", source = "competition.dateFrom")
    @Mapping(target = "dateTo", source = "competition.dateTo")
    CompetitionAnnouncementDto toApiAnnouncement(CompetitionAnnouncementDTO dto);

    List<CompetitionAnnouncementDto> toApiAnnouncements(List<CompetitionAnnouncementDTO> dtos);

    CompetitionParticipantDto toApiParticipant(CompetitionParticipantDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "gymId", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "competitionId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    BoulderDTO toDomainCompetitionBoulder(CompetitionBoulderRequest request);

    @Mapping(target = "id", source = "boulder.id")
    @Mapping(target = "competitionId", source = "boulder.competitionId")
    @Mapping(target = "wallImageId", source = "boulder.wallImageId")
    @Mapping(target = "name", source = "boulder.name")
    @Mapping(target = "grade", source = "boulder.grade")
    @Mapping(target = "description", source = "boulder.description")
    @Mapping(target = "points", source = "boulder.points")
    CompetitionBoulderDto toApiCompetitionBoulder(CompetitionBoulderDTO dto);

    List<CompetitionBoulderDto> toApiCompetitionBoulders(List<CompetitionBoulderDTO> dtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "participantId", ignore = true)
    @Mapping(target = "competitionId", ignore = true)
    @Mapping(target = "points", ignore = true)
    CompetitionResultDTO toDomainResult(BoulderResultRequest request);

    List<CompetitionResultDTO> toDomainResults(List<BoulderResultRequest> requests);

    @Mapping(target = "competitionId", source = "competition.id")
    @Mapping(target = "dateFrom", source = "competition.dateFrom")
    @Mapping(target = "dateTo", source = "competition.dateTo")
    @Mapping(target = "resultsOpenUntil", source = "competition")
    @Mapping(target = "section", source = "participant.section")
    @Mapping(target = "lastSnapshot", source = "participant.lastSnapshot")
    CompetitionUserStatusDto toApiUserStatus(CompetitionUserStatusDTO dto);

    List<CompetitionRankingEntryDto> toApiRanking(List<CompetitionRankingRecord> records);

    default OffsetDateTime toResultsOpenUntil(CompetitionDTO competition) {
        return competition == null
                ? null
                : toOffset(
                        competition
                                .getDateTo()
                                .plus(Duration.ofMinutes(competition.getGraceMinutes())));
    }

    /** Responses always carry UTC (Z); clients render the instant in their own zone. */
    default OffsetDateTime toOffset(Instant instant) {
        return instant == null ? null : instant.atOffset(ZoneOffset.UTC);
    }

    /** Requests may carry any offset; it is dropped once the absolute instant is known. */
    default Instant toInstant(OffsetDateTime dateTime) {
        return dateTime == null ? null : dateTime.toInstant();
    }

    default BigDecimal toBigDecimal(Float value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }
}
