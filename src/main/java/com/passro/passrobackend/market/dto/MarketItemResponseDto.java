package com.passro.passrobackend.market.dto;

import com.passro.passrobackend.market.entity.Market;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Schema(types = "object", description = "마켓 상품 응답")
public class MarketItemResponseDto {

    @Schema(description = "상품 ID", example = "1")
    private Long id;

    @Schema(description = "상품명", example = "커피 쿠폰")
    private String name;

    @Schema(description = "구매 가격(포인트)", example = "3000")
    private Long price;

    public static MarketItemResponseDto from(Market market) {
        return MarketItemResponseDto.builder()
                .id(market.getId())
                .name(market.getName())
                .price(market.getPrice())
                .build();
    }
}
