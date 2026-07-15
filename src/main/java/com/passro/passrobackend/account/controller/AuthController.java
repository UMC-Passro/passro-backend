package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AccountService accountService;

    @GetMapping("/mail")
    public APIResponse<Void> mailSend(@RequestParam String mail){
        BaseSuccessCode code = AccountSuccessCode.OK;
        accountService.sendMailMessage(mail);
        return APIResponse.onSuccess(code, null);
    }
}
