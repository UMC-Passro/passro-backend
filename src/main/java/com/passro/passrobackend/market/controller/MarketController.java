package com.passro.passrobackend.market.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.market.code.MarketSuccessCode;
import com.passro.passrobackend.market.dto.MarketItemResponseDto;
import com.passro.passrobackend.market.dto.MarketPurchaseResponseDto;
import com.passro.passrobackend.market.service.MarketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/market")
@Tag(name = "마켓", description = "포인트 상품 조회 및 구매 API")
public class MarketController {

    private final MarketService marketService;

    @GetMapping
    @Operation(summary = "마켓 상품 목록 조회", description = "포인트로 구매할 수 있는 상품을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공", useReturnTypeSchema = true)
    public APIResponse<List<MarketItemResponseDto>> getItems() {
        return APIResponse.onSuccess(MarketSuccessCode.OK, marketService.getItems());
    }

    @PostMapping("/{marketId}/purchase")
    @Operation(
            summary = "마켓 상품 구매",
            description = "로그인한 사용자의 포인트 잔액을 확인한 뒤 상품 가격만큼 차감합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상품 구매 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "상품 또는 포인트 계정을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class))),
            @ApiResponse(responseCode = "409", description = "보유 포인트 부족",
                    content = @Content(schema = @Schema(implementation = APIResponse.class)))
    })
    public APIResponse<MarketPurchaseResponseDto> purchase(
            @Parameter(hidden = true) @AuthenticationPrincipal(expression = "account") Account account,
            @Parameter(description = "마켓 상품 ID", example = "1") @PathVariable Long marketId) {
        return APIResponse.onSuccess(
                MarketSuccessCode.OK,
                marketService.purchase(account.getId(), marketId));
    }
}
