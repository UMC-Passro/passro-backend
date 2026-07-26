package com.passro.passrobackend.account.controller;

import com.passro.passrobackend.account.dto.SubwayApiResDTO;
import com.passro.passrobackend.account.exception.code.AccountSuccessCode;
import com.passro.passrobackend.account.service.AccountService;
import com.passro.passrobackend.account.service.SubwayApiService;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import com.passro.passrobackend.global.response.APIResponse;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
}
