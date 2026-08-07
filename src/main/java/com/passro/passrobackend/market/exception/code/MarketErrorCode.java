package com.passro.passrobackend.market.exception.code;

import com.passro.passrobackend.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MarketErrorCode implements BaseErrorCode {
    MARKET_NOT_FOUND(HttpStatus.NOT_FOUND, "MARKET404_1", "마켓 상품을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
