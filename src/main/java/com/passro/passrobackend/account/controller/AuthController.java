package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.AuthDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AccountService accountService;



    @PostMapping("/mail/send")
    public APIResponse<Void> mailSend(@Valid @RequestBody AuthDTO.SendMail dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessage(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/mail/confirm")
    public APIResponse<Void> confirmCode(@Valid @RequestBody AuthDTO.ConfirmCode dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.confirmCode(dto);
        return APIResponse.onSuccess(code, null);
    }

    @PostMapping("/signup")
    public APIResponse<Void> signup(@Valid @RequestBody AuthDTO.Signup dto){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.signup(dto);
        return APIResponse.onSuccess(code, null);
    }
}
