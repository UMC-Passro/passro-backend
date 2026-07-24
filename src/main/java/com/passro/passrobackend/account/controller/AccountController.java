package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.service.AccountService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Hidden
public class AccountController {

    private final AccountService accountService;

}
