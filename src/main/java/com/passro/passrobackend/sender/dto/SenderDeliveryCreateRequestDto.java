package com.passro.passrobackend.sender.dto;

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
    private String originAddress;
    private String destAddress;
    
    // DeliveryGoodInfo
    private String name;
    private Long price;
    private String size;
    private String picture;
    
    // Delivery memo
    private String memo;
}
