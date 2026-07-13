package com.passro.passrobackend.sender.dto;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.place.entity.Place;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class SenderDeliveryDto {
    private Account senderAccount;
    private Account shipperAccount;

    private Place originPlace;
    private Place destPlace;

    private DeliveryState deliveryState;
    private String memo;

    public static SenderDeliveryDto fromDelivery(Delivery delivery) {
        return SenderDeliveryDto.builder()
                .senderAccount(delivery.getSender())
                .shipperAccount(delivery.getShipper())
                .originPlace(delivery.getOrigin())
                .destPlace(delivery.getDest())
                .memo(delivery.getMemo())
                .deliveryState(delivery.getStatus())
                .build();
        }
}
