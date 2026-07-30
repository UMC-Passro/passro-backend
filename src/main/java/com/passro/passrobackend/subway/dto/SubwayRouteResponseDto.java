package com.passro.passrobackend.subway.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubwayRouteResponseDto {

    private int shortestDistance;
    private int transferCount;
    private List<SubwayStationResponseDto> stations;
}
