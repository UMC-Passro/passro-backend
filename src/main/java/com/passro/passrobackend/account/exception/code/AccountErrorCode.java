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

    MAIL_NOT_CONFIRM(HttpStatus.BAD_REQUEST,
            "ACCOUNT400_3",
            "인증되지 않은 이메일입니다."),

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST,
            "ACCOUNT400_4",
            "사용 중인 이메일입니다."),

    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST,
            "ACCOUNT400_5",
            "사용 중인 닉네임입니다.."),

    INVALID_EMAIL_DOMAIN(HttpStatus.BAD_REQUEST,
            "Account400_6",
            "학생용 이메일이 아닙니다."),

    MAIL_RESEND_TOO_FAST(HttpStatus.TOO_MANY_REQUESTS,
            "Account400_7",
            "잠시 후 다시 시도해주세요."),

    ;


    private final HttpStatus status;
    private final String message;
    private final String code;
}
