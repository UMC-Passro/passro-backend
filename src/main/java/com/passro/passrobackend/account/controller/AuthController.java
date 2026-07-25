package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.authDTO.AuthReqDTO;
import com.passro.passrobackend.account.dto.authDTO.AuthResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.configuration.security.CustomUserDetails;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;



    @PostMapping("/mail/send")
    public APIResponse<Void> mailSend(@Valid @RequestBody AuthReqDTO.SendMail dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessage(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/mail/confirm")
    public APIResponse<Void> confirmCode(@Valid @RequestBody AuthReqDTO.ConfirmCode dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.confirmCode(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/signup")
    public APIResponse<Void> signup(@Valid @RequestBody AuthReqDTO.Signup dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.signup(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/login")
    public APIResponse<AuthResDTO.TokenResponse> login(@Valid @RequestBody AuthReqDTO.Login dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return APIResponse.onSuccess(code, accountService.login(dto));
    }

    @GetMapping("/logout")
    public APIResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.logout(userDetails.getAccountId());
        return APIResponse.onSuccess(code, null);
    }

    @GetMapping("/reissue")
    public APIResponse<AuthResDTO.TokenResponse> reissue(@RequestHeader("Refresh-Token") String refreshToken){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return APIResponse.onSuccess(code, accountService.reissueToken(refreshToken));
    }
}
