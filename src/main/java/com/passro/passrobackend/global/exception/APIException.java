package com.passro.passrobackend.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIException extends RuntimeException{
    private final String code;
    private final String message;

    private final Object payload;
}
