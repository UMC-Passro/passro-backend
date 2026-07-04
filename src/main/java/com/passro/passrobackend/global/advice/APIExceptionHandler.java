package com.passro.passrobackend.global.advice;

import com.passro.passrobackend.global.exception.APIException;
import com.passro.passrobackend.global.response.APIResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class APIExceptionHandler {

    @ExceptionHandler(APIException.class)
    public APIResponse handleAPIException(APIException e) {
        return new APIResponse<>(false, e.getCode(), e.getMessage(), e.getPayload());
    }

}
