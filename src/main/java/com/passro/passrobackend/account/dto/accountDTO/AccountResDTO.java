package com.passro.passrobackend.account.dto.accountDTO;

import lombok.AllArgsConstructor;
import lombok.Getter;

public class AccountResDTO {

    @Getter
    @AllArgsConstructor
    public static class ShipperMyPage{
        private String nickname;
        private Long deliveryCount;
        private Long point;
        private double rating;
    }

    @Getter
    @AllArgsConstructor
    public static class SenderMyPage{
        private String nickname;
        private Long deliveryCount;
        private Long point;
    }
}
