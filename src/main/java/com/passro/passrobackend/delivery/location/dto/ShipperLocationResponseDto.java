package com.passro.passrobackend.delivery.location.dto;

import com.passro.passrobackend.delivery.location.model.ShipperLocation;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShipperLocationResponseDto(
        BigDecimal latitude,
        BigDecimal longitude,
        Long placeId,
        LocalDateTime updatedAt
) {
    public static ShipperLocationResponseDto from(ShipperLocation location) {
        return new ShipperLocationResponseDto(
                location.latitude(),
                location.longitude(),
                location.placeId(),
                location.updatedAt());
    }
}
