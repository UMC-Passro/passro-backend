package com.passro.passrobackend.subway.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.subway.graph.SubwayNode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubwayStationResponseDto {

    @Schema(description = "Place ID", example = "101")
    private Long id;

    @Schema(description = "권역명. 최단 경로 응답에서 제공됩니다.", example = "수도권", nullable = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String region;

    @Schema(description = "지하철 노선명", example = "2호선")
    private String routeName;

    @Schema(description = "지하철역명", example = "강남")
    private String stationName;

    public static SubwayStationResponseDto from(SubwayNode node) {
        return new SubwayStationResponseDto(
                node.getId(),
                node.getRegion(),
                node.getRoute(),
                node.getName());
    }

    public static SubwayStationResponseDto from(Place place) {
        return new SubwayStationResponseDto(
                place.getId(),
                null,
                place.getSubwayRouteName(),
                place.getSubwayStationName());
    }
}
