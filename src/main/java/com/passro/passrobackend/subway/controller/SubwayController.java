package com.passro.passrobackend.subway.controller;

import com.passro.passrobackend.global.exception.APIException;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.subway.code.SubwayErrorCode;
import com.passro.passrobackend.subway.code.SubwaySuccessCode;
import com.passro.passrobackend.subway.dto.SubwayRouteRequestDto;
import com.passro.passrobackend.subway.dto.SubwayRouteResponseDto;
import com.passro.passrobackend.subway.service.SubwayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/subway")
@RequiredArgsConstructor
@Tag(name = "지하철", description = "지하철 최단 경로 조회 API")
public class SubwayController {

    private final SubwayService subwayService;

    @PostMapping("/routes/shortest")
    @Operation(
            summary = "지하철 최단 경로 조회",
            description = "출발역부터 경유역을 입력 순서대로 지나 도착역까지 이동하는 최단 경로를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "최단 경로 조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "404", description = "역 또는 경로를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public APIResponse<SubwayRouteResponseDto> findShortestRoute(
            @Valid @RequestBody SubwayRouteRequestDto request) {
        try {
            SubwayRouteResponseDto route = subwayService.findShortestRouteByPlaceIds(
                    request.getOriginPlaceId(),
                    request.getWaypointPlaceIds(),
                    request.getDestinationPlaceId());
            return APIResponse.onSuccess(SubwaySuccessCode.OK, route);
        } catch (IllegalArgumentException exception) {
            throw new APIException(SubwayErrorCode.PLACE_NOT_FOUND);
        } catch (IllegalStateException exception) {
            throw new APIException(SubwayErrorCode.ROUTE_NOT_FOUND);
        }
    }
}
