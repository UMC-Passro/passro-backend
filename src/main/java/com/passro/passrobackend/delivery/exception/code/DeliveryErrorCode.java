package com.passro.passrobackend.delivery.exception.code;

import com.passro.passrobackend.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum DeliveryErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "DELIVERY404_1",
            "해당 배송을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
