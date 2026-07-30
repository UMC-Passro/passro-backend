package com.passro.passrobackend.sender.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SenderDeliveryCreateRequestDto {
    @NotNull(message = "출발역 Place ID를 입력하세요.")
    @Positive(message = "출발역 Place ID는 양수여야 합니다.")
    private Long sourceStationId;

    @NotNull(message = "도착역 Place ID를 입력하세요.")
    @Positive(message = "도착역 Place ID는 양수여야 합니다.")
    private Long destinationStationId;

    // Delivery
    @NotBlank(message = "물품명을 입력하세요.")
    private String name;

    // DeliveryGoodInfo
    @NotNull(message = "물품 가격을 입력하세요.")
    @PositiveOrZero(message = "물품 가격은 0 이상이어야 합니다.")
    private Long price;

    @NotBlank(message = "물품 크기를 입력하세요.")
    private String size;
    private String picture;

    // Delivery memo
    private String memo;

    // Delivery point
    @PositiveOrZero(message = "기본 포인트는 0 이상이어야 합니다.")
    private Long basePoint;

    @PositiveOrZero(message = "거리 포인트는 0 이상이어야 합니다.")
    private Long distancePoint;

    @PositiveOrZero(message = "무게 포인트는 0 이상이어야 합니다.")
    private Long weightPoint;
}
