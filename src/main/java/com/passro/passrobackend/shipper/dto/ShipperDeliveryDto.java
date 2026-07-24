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

    private SenderInfo senderInfo;
    private ShipperInfo shipperInfo;

    private Place originPlace;
    private Place destPlace;

    private DeliveryState deliveryState;
    private String memo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SenderInfo {
        private String name;
        private String picture;
        private Place place;

        public static SenderInfo fromAccount(Account account) {
            if (account == null) {
                return null;
            }

            return SenderInfo.builder()
                    .name(account.getName())
                    .picture(account.getPicture())
                    .place(account.getPlace_id())
                    .build();
        }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ShipperInfo {
        private String name;
        private String picture;
        private Place place;

        public static ShipperInfo fromAccount(Account account) {
            if (account == null) {
                return null;
            }

            return ShipperInfo.builder()
                    .name(account.getName())
                    .picture(account.getPicture())
                    .place(account.getPlace_id())
                    .build();
        }
    }

    public static ShipperDeliveryDto fromDelivery(Delivery delivery) {
        return ShipperDeliveryDto.builder()
                .id(delivery.getId())
                .senderInfo(SenderInfo.fromAccount(delivery.getSender()))
                .shipperInfo(ShipperInfo.fromAccount(delivery.getShipper()))
                .originPlace(delivery.getOrigin())
                .destPlace(delivery.getDest())
                .memo(delivery.getMemo())
                .deliveryState(delivery.getStatus())
                .build();
        }
}
