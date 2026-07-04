package com.passro.passrobackend.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIResponse<T> {
    private final boolean isSuccess;

    private final String code;
    private final String message;

    private T result;
}
