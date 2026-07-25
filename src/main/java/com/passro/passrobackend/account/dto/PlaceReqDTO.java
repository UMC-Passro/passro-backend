package com.passro.passrobackend.account.dto;

import lombok.Getter;

import java.util.List;
import java.util.Map;

public class PlaceReqDTO {

    private List<PlaceVisit> places;

    @Getter
    public static class PlaceVisit {
        private Long placeId;
        private Integer order;
    }
}
