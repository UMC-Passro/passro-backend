package com.passro.passrobackend.account.dto.authDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AuthResDTO {

    @Getter
    @AllArgsConstructor
    public static class TokenResponse {
        private String accessToken;
        private String refreshToken;
    }


    @AllArgsConstructor
    public static class ShipperMyPage{
        private String nickname;
        private Long deliveryCount;
        private Long point;
        private double rating;
    }
}
