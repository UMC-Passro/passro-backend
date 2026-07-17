package com.passro.passrobackend.account.exception.code;

import com.passro.passrobackend.global.code.BaseErrorCode;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum AccountErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "ACCOUNT404_1",
            "해당 계정을 찾을 수 없습니다."),

    MAIL_CODE_EXPIRED(HttpStatus.BAD_REQUEST,
            "ACCOUNT400_1",
            "인증 코드가 만료되었거나 존재하지 않습니다."),

    MAIL_CODE_MISMATCH(HttpStatus.BAD_REQUEST,
            "ACCOUNT400_2",
            "인증 코드가 일치하지 않습니다."),

    ;


    private final HttpStatus status;
    private final String message;
    private final String code;
}
