package com.passro.passrobackend.notification.code;

import com.passro.passrobackend.global.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum NotificationErrorCode implements BaseErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND,
            "NOTIFICATION404_1",
            "해당 알림을 찾을 수 없습니다."),

    FORBIDDEN(HttpStatus.FORBIDDEN,
            "NOTIFICATION403_1",
            "본인의 알림만 조작할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
