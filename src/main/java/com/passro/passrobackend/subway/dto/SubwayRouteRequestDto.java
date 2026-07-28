package com.passro.passrobackend.subway.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SubwayRouteRequestDto {

    @NotNull(message = "출발역 Place ID는 필수입니다.")
    private Long originPlaceId;

    private List<@NotNull(message = "경유역 Place ID는 null일 수 없습니다.") Long> waypointPlaceIds;

    @NotNull(message = "도착역 Place ID는 필수입니다.")
    private Long destinationPlaceId;
}
