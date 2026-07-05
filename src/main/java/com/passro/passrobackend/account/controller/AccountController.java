package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.global.response.APIResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

}
