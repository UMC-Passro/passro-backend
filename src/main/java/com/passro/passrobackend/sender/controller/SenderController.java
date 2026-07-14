package com.passro.passrobackend.sender.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.sender.code.SenderSuccessCode;
import com.passro.passrobackend.sender.dto.SenderDeliveryCreateRequestDto;
import com.passro.passrobackend.sender.dto.SenderDeliveryDetailDto;
import com.passro.passrobackend.sender.dto.SenderDeliveryListDto;
import com.passro.passrobackend.sender.dto.SenderPaymentAmountDto;
import com.passro.passrobackend.sender.service.SenderQueryService;
import com.passro.passrobackend.sender.service.SenderCommandService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sender")
@RequiredArgsConstructor
public class SenderController {

    private final SenderQueryService senderQueryService;
    private final SenderCommandService senderCommandService;

    // 발송자 배송 조회
    @GetMapping
    public APIResponse<List<SenderDeliveryListDto>> getSenders(@AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderQueryService.getSenders(account));
    }

    // 발송자 배송 단건 조회
    @GetMapping("/{deliveryId}")
    public APIResponse<SenderDeliveryDetailDto> getSenderById(@AuthenticationPrincipal Account account,
                                                             @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderQueryService.getDeliveryDetail(account, deliveryId));
    }

    // 결제 금액 조회
    @GetMapping("/{deliveryId}/payment")
    public APIResponse<SenderPaymentAmountDto> getPaymentAmount(@AuthenticationPrincipal Account account,
                                                                @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderQueryService.getPaymentAmount(account, deliveryId));
    }

    // 배송 요청
    @PostMapping
    public APIResponse<String> createDelivery(@AuthenticationPrincipal Account account,
                                              @RequestBody SenderDeliveryCreateRequestDto request) {
        Long deliveryId = senderCommandService.createDelivery(account, request);
        return APIResponse.onSuccess(SenderSuccessCode.CREATED, deliveryId.toString());
    }

    // 발송 완료 처리
    @PatchMapping("/{deliveryId}/complete")
    public APIResponse<String> completeDelivery(@AuthenticationPrincipal Account account,
                                                @PathVariable Long deliveryId) {
        senderCommandService.completeDelivery(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    // 발송 약관 동의
    @PatchMapping("/{deliveryId}/terms")
    public APIResponse<String> agreeTerms(@AuthenticationPrincipal Account account,
                                          @PathVariable Long deliveryId) {
        senderCommandService.agreeTerms(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    // 발송 요청 취소
    @PatchMapping("/{deliveryId}/cancel")
    public APIResponse<String> cancelDelivery(@AuthenticationPrincipal Account account,
                                              @PathVariable Long deliveryId) {
        senderCommandService.cancelDelivery(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

}
