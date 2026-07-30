package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.accountDTO.AccountReqDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.configuration.security.CustomUserDetails;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/mypage/shipper")
    public ResponseEntity<APIResponse<AccountResDTO.ShipperMyPage>> shipperPage(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, accountService.myShipperPage(userDetails.getAccountId())));
    }

    @GetMapping("/mypage/sender")
    public ResponseEntity<APIResponse<AccountResDTO.SenderMyPage>> senderPage(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, accountService.mySenderPage(userDetails.getAccountId())));
    }

    @PostMapping("/mypage/edit/myInfo")
    public ResponseEntity<APIResponse<Void>> editNickname(@Valid @RequestBody AccountReqDTO.EditMyInfo dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.editMyInfo(dto, userDetails.getAccountId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, null));
    }

    @PatchMapping("/mypage/edit/password/mail")
    public APIResponse<Void> sendPasswordEditMail(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessageAndEditPassword(userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }

    @PatchMapping("/mypage/edit/password")
    public APIResponse<Void> editPassword(@Valid @RequestBody AccountReqDTO.EditPassword dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.codeCodeConfirmAndEditPassword(dto, userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }
}
