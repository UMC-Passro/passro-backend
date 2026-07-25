package com.passro.passrobackend.account.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AuthResDTO {

    @Getter
    @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
    }
}
