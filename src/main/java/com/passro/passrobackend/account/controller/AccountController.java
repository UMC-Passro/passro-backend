package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountReqDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.account.service.SubwayApiService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.configuration.security.CustomUserDetails;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/subway")
public class AccountController {

    private final AccountService accountService;
    private final SubwayApiService subwayApiService;


    @GetMapping("/search")
    public APIResponse<List<SubwayApiResDTO.Item>> search(
            @RequestParam
            @Pattern(regexp = "^[가-힣0-9]+$", message = "검색어는 한글과 숫자만 입력 가능합니다.")
            String keyword) {
        BaseSuccessCode code = AccountSuccessCode.OK;
        return APIResponse.onSuccess(code, subwayApiService.searchStation(keyword));
    }

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

    @PostMapping("/mypage/edit/nickname")
    public ResponseEntity<APIResponse<Void>> editNickname(@RequestBody AccountReqDTO.EditNickname dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.editNickname(dto, userDetails.getAccountId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, CacheControl.noStore().getHeaderValue())
                .body(APIResponse.onSuccess(code, null));
    }

    @PostMapping("/mypage/edit/password/mail")
    public APIResponse<Void> sendPasswordEditMail(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessageAndEditPassword(userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/mypage/edit/password")
    public APIResponse<Void> editPassword(@Valid @RequestBody AccountReqDTO.EditPassword dto, @AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.codeCodeConfirmAndEditPassword(dto, userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }
}
