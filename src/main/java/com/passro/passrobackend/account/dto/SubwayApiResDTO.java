package com.passro.passrobackend.account.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

import java.util.List;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class SubwayApiResDTO {

    private Response response;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Response{
        private Body body;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Body{
        private Items items;
        private int totalCount;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Items{
        private List<Item> item;
    }
    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item{
        private String subwayStationId;
        private String subwayStationName;
        private String subwayRouteName;
    }
}
