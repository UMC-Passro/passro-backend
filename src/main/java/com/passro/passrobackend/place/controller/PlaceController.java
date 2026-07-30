package com.passro.passrobackend.place.controller;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountReqDTO;
import com.passro.passrobackend.account.dto.accountDTO.AccountResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.configuration.security.CustomUserDetails;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import com.passro.passrobackend.place.service.PlaceService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/subway")
public class PlaceController {

    private final PlaceService placeService;
    private final AccountService accountService;

    @GetMapping("/search")
    public APIResponse<List<SubwayApiResDTO.Item>> search(
            @RequestParam
            @Pattern(regexp = "^[가-힣0-9]+$", message = "검색어는 한글과 숫자만 입력 가능합니다.")
            String keyword) {
        BaseSuccessCode code = AccountSuccessCode.OK;
        List<SubwayApiResDTO.Item> result = placeService.searchByKeyword(keyword).stream()
                .map(place -> new SubwayApiResDTO.Item(
                        place.getId(),
                        place.getSubwayStationName(),
                        place.getSubwayRouteName()))
                .toList();
        return APIResponse.onSuccess(code, result);
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
