package com.passro.passrobackend.sender.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.sender.code.SenderSuccessCode;
import com.passro.passrobackend.sender.dto.SenderDeliveryDetailDto;
import com.passro.passrobackend.sender.dto.SenderPaymentAmountDto;
import com.passro.passrobackend.sender.service.SenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sender")
@RequiredArgsConstructor
public class SenderController {

    private final SenderService senderService;

    // 발송자 발송 조회
    @GetMapping
    public APIResponse<String> getSenders(@AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    // 발송자 배송 단건 조회
    @GetMapping("/{deliveryId}")
    public APIResponse<SenderDeliveryDetailDto> getSenderById(@AuthenticationPrincipal Account account,
                                                             @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderService.getDeliveryDetail(account, deliveryId));
    }

    // 결제 금액 조회
    @GetMapping("/{deliveryId}/payment")
    public APIResponse<SenderPaymentAmountDto> getPaymentAmount(@AuthenticationPrincipal Account account,
                                                                @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(SenderSuccessCode.OK, senderService.getPaymentAmount(account, deliveryId));
    }

    // 배송 요청
    @PostMapping
    public APIResponse<String> createDelivery(@AuthenticationPrincipal Account account) {
        return APIResponse.onSuccess(SenderSuccessCode.CREATED, null);
    }

    // 발송 완료 처리
    @PatchMapping("/{deliveryId}/complete")
    public APIResponse<String> completeDelivery(@AuthenticationPrincipal Account account,
                                                @PathVariable Long deliveryId) {
        senderService.completeDelivery(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    // 발송 약관 동의
    @PatchMapping("/{deliveryId}/terms")
    public APIResponse<String> agreeTerms(@AuthenticationPrincipal Account account,
                                          @PathVariable Long deliveryId) {
        senderService.agreeTerms(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

    // 발송 요청 취소
    @PatchMapping("/{deliveryId}/cancel")
    public APIResponse<String> cancelDelivery(@AuthenticationPrincipal Account account,
                                              @PathVariable Long deliveryId) {
        senderService.cancelDelivery(account, deliveryId);
        return APIResponse.onSuccess(SenderSuccessCode.OK, null);
    }

}
