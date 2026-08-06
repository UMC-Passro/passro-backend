package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.accountDTO.AccountReqDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.account.service.MailSenderService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.configuration.security.CustomUserDetails;
import com.passro.passrobackend.global.response.APIResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "계정", description = "프로필 조회 및 수정")
public class AccountController {

    private final AccountService accountService;
    private final MailSenderService mailSenderService;

    @GetMapping("/mypage/shipper")
    @Operation(summary = "배송기사 마이페이지 조회", description = "마이페이지를 배송기사 기준으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "배송기사 마이페이지 조회 성공", useReturnTypeSchema = true)
    public ResponseEntity<APIResponse<AccountResDTO.ShipperMyPage>> shipperPage(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, accountService.myShipperPage(userDetails.getAccountId())));
    }

    @GetMapping("/mypage/sender")
    @Operation(summary = "발송자 마이페이지 조회", description = "마이페이지를 발송자 기준으로 조회합니다.")
    @ApiResponse(responseCode = "200", description = "발송자 마이페이지 조회 성공", useReturnTypeSchema = true)
    public ResponseEntity<APIResponse<AccountResDTO.SenderMyPage>> senderPage(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, accountService.mySenderPage(userDetails.getAccountId())));
    }

    @PatchMapping("/mypage/edit/myInfo")
    @Operation(summary = "마이페이지 수정", description = "마이페이지에서 원하는 정보를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "마이페이지 수정 성공", useReturnTypeSchema = true)
    public ResponseEntity<APIResponse<Void>> editNickname(@Valid @RequestBody AccountReqDTO.EditMyInfo dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.editMyInfo(dto, userDetails.getAccountId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, null));
    }

    @PatchMapping("/mypage/edit/password/mail")
    @ApiResponse(responseCode = "200", description = "메일 요청 성공", useReturnTypeSchema = true)
    public APIResponse<Void> sendPasswordEditMail(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        mailSenderService.sendMailMessageEditPassword(userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }

    @PatchMapping("/mypage/edit/password")
    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호와 새로운 비밀번호를 입력하여 변경합니다.")
    @ApiResponse(responseCode = "200", description = "비밀번호 변경 성공", useReturnTypeSchema = true)
    public APIResponse<Void> editPassword(@Valid @RequestBody AccountReqDTO.EditPassword dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.codeCodeConfirmAndEditPassword(dto, userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }
}
