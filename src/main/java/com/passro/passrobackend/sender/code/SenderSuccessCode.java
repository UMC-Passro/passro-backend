package com.passro.passrobackend.sender.code;

import com.passro.passrobackend.global.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum SenderSuccessCode implements BaseSuccessCode {
    OK(HttpStatus.OK,
            "ACCOUNT200_1",
            "요청 성공.");

    private final HttpStatus status;
    private final String message;
    private final String code;

}
