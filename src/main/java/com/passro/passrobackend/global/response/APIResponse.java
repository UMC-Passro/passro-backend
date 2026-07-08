package com.passro.passrobackend.global.response;

import com.passro.passrobackend.global.code.BaseErrorCode;
import com.passro.passrobackend.global.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class APIResponse<T> {
    private final boolean isSuccess;

    private final String code;
    private final String message;

    private T result;

    public static <T> APIResponse<T> onSuccess(BaseSuccessCode code, T result){
        return new APIResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    public static <T> APIResponse<T> onFailure(BaseErrorCode code, T result){
        return new APIResponse<>(false, code.getCode(), code.getMessage(), result);
    }
}
