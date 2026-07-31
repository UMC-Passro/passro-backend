package com.passro.passrobackend.point.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointHistoryResponseDto {

    private Long currentPoint;
    private List<PointLogResponseDto> pointLogs;
}
