package com.passro.passrobackend.sender.dto;

import com.passro.passrobackend.delivery.enums.DeliveryState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SenderDeliveryListDto {
    private Long deliveryId;
    private String goodName;
    private String originAddress;
    private String destAddress;
    private DeliveryState status;
}
