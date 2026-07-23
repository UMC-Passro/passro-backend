package com.passro.passrobackend.shipper.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.shipper.code.ShipperSuccessCode;
import com.passro.passrobackend.shipper.dto.ShipperDeliveryDto;
import com.passro.passrobackend.shipper.service.ShipperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.passro.passrobackend.global.configuration.SwaggerErrorExamples.DELIVERY_NOT_FOUND;
import static com.passro.passrobackend.global.configuration.SwaggerSuccessExamples.SHIPPER_DETAIL;
import static com.passro.passrobackend.global.configuration.SwaggerSuccessExamples.SHIPPER_LIST;
import static com.passro.passrobackend.global.configuration.SwaggerSuccessExamples.SHIPPER_OK;

@RestController
@RequestMapping("/shipper")
@RequiredArgsConstructor
@Tag(name = "배송기사", description = "배송기사의 배송 조회 및 배송 상태 변경 API")
public class ShipperController {
    private final ShipperService shipperService;

    @GetMapping("/matched")
    @ResponseBody
    @Operation(summary = "매칭 대기 배송 목록 조회", description = "현재 배송기사를 기다리는 배송 요청 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true,
            content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "매칭 대기 배송 조회 성공", value = SHIPPER_LIST)))
    public APIResponse<List<ShipperDeliveryDto>> listMatched(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(ShipperSuccessCode.OK, shipperService.listMatchRequested().stream().map(ShipperDeliveryDto::fromDelivery).toList());
    }

    @GetMapping("/")
    @ResponseBody
    @Operation(summary = "내 배송 목록 조회", description = "로그인한 배송기사에게 배정된 배송 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true,
            content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "내 배송 목록 조회 성공", value = SHIPPER_LIST)))
    public APIResponse<List<ShipperDeliveryDto>> listDelivery(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account) {
         return APIResponse.onSuccess(ShipperSuccessCode.OK, shipperService.listAllByShipper(account).stream().map(ShipperDeliveryDto::fromDelivery).toList());
    }

    @GetMapping("/{deliveryId}/")
    @ResponseBody
    @Operation(summary = "배송 상세 조회", description = "배송 ID로 배송 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "배송 상세 조회 성공", value = SHIPPER_DETAIL))),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<ShipperDeliveryDto> getDelivery(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Parameter(description = "배송 ID", example = "1") @PathVariable("deliveryId") Long deliveryId) {
        Delivery delivery = shipperService.getDeliveryById(account, deliveryId);

        return APIResponse.onSuccess(ShipperSuccessCode.OK, ShipperDeliveryDto.fromDelivery(delivery));
    }

    @PatchMapping("/{deliveryId}/matched")
    @ResponseBody
    @Operation(summary = "배송 매칭 수락", description = "배송 요청을 수락하고 배송기사를 배정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "매칭 수락 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "매칭 수락 성공", value = SHIPPER_OK))),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<Void> matchAccept(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Parameter(description = "배송 ID", example = "1") @PathVariable("deliveryId") Long deliveryId) {
        shipperService.matchAccept(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }

    @PatchMapping("/{deliveryId}/acquire")
    @ResponseBody
    @Operation(summary = "물품 인수 처리", description = "물품을 인수하고 배송 상태를 배송 중으로 변경합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "인수 처리 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "물품 인수 처리 성공", value = SHIPPER_OK))),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<Void> acquireAccept(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Parameter(description = "배송 ID", example = "1") @PathVariable("deliveryId") Long deliveryId) {
        shipperService.acquireAccept(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }

    @PatchMapping("/{deliveryId}/confirm")
    @ResponseBody
    @Operation(summary = "배송 완료 확인 요청", description = "발송자에게 배송 완료 확인을 요청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "확인 요청 성공", useReturnTypeSchema = true,
                    content = @Content(examples = @ExampleObject(name = "SHIPPER200_1", summary = "배송 완료 확인 요청 성공", value = SHIPPER_OK))),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<Void> acquireConfirm(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Parameter(description = "배송 ID", example = "1") @PathVariable("deliveryId") Long deliveryId) {
        shipperService.acquireConfirm(account, deliveryId);
        return APIResponse.onSuccess(ShipperSuccessCode.OK, null);
    }
}
