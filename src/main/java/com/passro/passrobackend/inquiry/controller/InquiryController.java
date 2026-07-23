package com.passro.passrobackend.inquiry.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.inquiry.code.InquirySuccessCode;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 작성
    @PostMapping
    public APIResponse<InquiryResponseDto> createInquiry(@AuthenticationPrincipal Account account,
                                                         @Valid @RequestBody InquiryCreateRequestDto request) {
        return APIResponse.onSuccess(InquirySuccessCode.CREATED, inquiryService.createInquiry(account, request));
    }

    // 문의 조회 (배송별 문의 목록)
    @GetMapping("/{deliveryId}")
    public APIResponse<List<InquiryResponseDto>> getInquiriesByDelivery(@AuthenticationPrincipal Account account,
                                                                        @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(InquirySuccessCode.OK, inquiryService.getInquiriesByDelivery(deliveryId));
    }
}