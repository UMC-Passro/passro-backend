package com.passro.passrobackend.inquiry.controller;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.global.response.APIResponse;
import com.passro.passrobackend.inquiry.code.InquirySuccessCode;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

import static com.passro.passrobackend.global.configuration.SwaggerErrorExamples.COMMON_VALIDATION;
import static com.passro.passrobackend.global.configuration.SwaggerErrorExamples.DELIVERY_NOT_FOUND;

@RestController
@RequestMapping("/inquiry")
@RequiredArgsConstructor
@Tag(name = "문의", description = "배송 문의 작성 및 조회 API")
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 작성
    @PostMapping
    @Operation(summary = "배송 문의 작성", description = "특정 배송에 대한 문의를 작성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 작성 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "COMMON400", summary = "요청 값 검증 실패", value = COMMON_VALIDATION))),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<InquiryResponseDto> createInquiry(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Valid @RequestBody InquiryCreateRequestDto request) {
        return APIResponse.onSuccess(InquirySuccessCode.CREATED, inquiryService.createInquiry(account, request));
    }

    // 문의 조회 (배송별 문의 목록)
    @GetMapping("/{deliveryId}")
    @Operation(summary = "배송 문의 목록 조회", description = "특정 배송의 문의 목록을 최신순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공", useReturnTypeSchema = true),
            @ApiResponse(responseCode = "404", description = "배송 정보를 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = APIResponse.class),
                            examples = @ExampleObject(name = "DELIVERY404_1", summary = "배송 정보 없음", value = DELIVERY_NOT_FOUND)))
    })
    public APIResponse<List<InquiryResponseDto>> getInquiriesByDelivery(
            @Parameter(hidden = true) @AuthenticationPrincipal Account account,
            @Parameter(description = "배송 ID", example = "1") @PathVariable Long deliveryId) {
        return APIResponse.onSuccess(InquirySuccessCode.OK, inquiryService.getInquiriesByDelivery(deliveryId));
    }
}
