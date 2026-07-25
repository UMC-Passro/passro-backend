package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.account.service.SubwayApiService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final SubwayApiService subwayApiService;


    @GetMapping("/subway/search")
    public APIResponse<List<SubwayApiResDTO.Item>> search(@RequestParam String keyword){
        BaseSuccessCode code = AccountSuccessCode.OK;
        return APIResponse.onSuccess(code, subwayApiService.searchStation(keyword));
    }
}
