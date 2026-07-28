package com.passro.passrobackend.subway.dto;

import com.passro.passrobackend.subway.graph.SubwayNode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubwayStationResponseDto {

    private Long placeId;
    private String region;
    private String routeName;
    private String stationName;

    public static SubwayStationResponseDto from(SubwayNode node) {
        return new SubwayStationResponseDto(
                node.getId(),
                node.getRegion(),
                node.getRoute(),
                node.getName());
    }
}
