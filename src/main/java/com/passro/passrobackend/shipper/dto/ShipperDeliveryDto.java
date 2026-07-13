package com.passro.passrobackend.shipper.dto;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.place.entity.Place;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ShipperDeliveryDto {
    private Long id;

    private Account senderAccount;
    private Account shipperAccount;

    private Place originPlace;
    private Place destPlace;

    private DeliveryState deliveryState;
    private String memo;

    public static ShipperDeliveryDto fromDelivery(Delivery delivery) {
        return ShipperDeliveryDto.builder()
                .id(delivery.getId())
                .senderAccount(delivery.getSender())
                .shipperAccount(delivery.getShipper())
                .originPlace(delivery.getOrigin())
                .destPlace(delivery.getDest())
                .memo(delivery.getMemo())
                .deliveryState(delivery.getStatus())
                .build();
        }
}
