package com.passro.passrobackend.point.dto;

import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.place.entity.Place;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointDeliveryResponseDto {

    private Long id;
    private String name;
    private PlaceResponseDto origin;
    private PlaceResponseDto destination;
    private DeliveryState status;
    private String memo;

    public static PointDeliveryResponseDto from(Delivery delivery) {
        return PointDeliveryResponseDto.builder()
                .id(delivery.getId())
                .name(delivery.getName())
                .origin(PlaceResponseDto.from(delivery.getOrigin()))
                .destination(PlaceResponseDto.from(delivery.getDest()))
                .status(delivery.getStatus())
                .memo(delivery.getMemo())
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class PlaceResponseDto {

        private Long id;
        private String subwayRouteName;
        private String subwayStationName;

        public static PlaceResponseDto from(Place place) {
            if (place == null) {
                return null;
            }

            return PlaceResponseDto.builder()
                    .id(place.getId())
                    .subwayRouteName(place.getSubwayRouteName())
                    .subwayStationName(place.getSubwayStationName())
                    .build();
        }
    }
}
