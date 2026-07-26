package com.passro.passrobackend.inquiry.code;

import com.passro.passrobackend.global.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum InquirySuccessCode implements BaseSuccessCode {

    OK(HttpStatus.OK,
            "INQUIRY200_1",
            "문의 조회 성공."),

    CREATED(HttpStatus.CREATED,
            "INQUIRY201_1",
            "문의 등록 성공.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
