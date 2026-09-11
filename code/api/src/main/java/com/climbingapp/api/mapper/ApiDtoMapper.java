package com.climbingapp.api.mapper;

import com.climbingapp.api.dto.BoulderDetailDto;
import com.climbingapp.api.dto.BoulderDto;
import com.climbingapp.api.dto.BoulderPageResponse;
import com.climbingapp.api.dto.CreateHoldRequest;
import com.climbingapp.api.dto.GymDto;
import com.climbingapp.api.dto.GymPageResponse;
import com.climbingapp.api.dto.HoldDto;
import com.climbingapp.api.dto.WallImageDetail;
import com.climbingapp.api.dto.WallImagePageResponse;
import com.climbingapp.api.dto.WallImageSummary;
import com.climbingapp.domain.dto.BoulderDTO;
import com.climbingapp.domain.dto.GymDTO;
import com.climbingapp.domain.dto.HoldDTO;
import com.climbingapp.domain.dto.WallImageDTO;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiDtoMapper {

    GymDto toApiGym(GymDTO dto);

    GymPageResponse toGymPage(Page<GymDTO> page);

    BoulderDto toApiBoulder(BoulderDTO dto);

    BoulderPageResponse toBoulderPage(Page<BoulderDTO> page);

    @Mapping(target = "id", source = "boulder.id")
    @Mapping(target = "holds", source = "holds")
    BoulderDetailDto toBoulderDetail(BoulderDTO boulder, List<HoldDTO> holds);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "boulderId", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "updatedDate", ignore = true)
    @Mapping(target = "XRatio", source = "xRatio")
    @Mapping(target = "YRatio", source = "yRatio")
    HoldDTO toDomainHold(CreateHoldRequest request);

    @Mapping(target = "xRatio", source = "XRatio")
    @Mapping(target = "yRatio", source = "YRatio")
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

    default OffsetDateTime toOffset(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    default BigDecimal toBigDecimal(Float value) {
        return value == null ? null : BigDecimal.valueOf(value.doubleValue());
    }
}
