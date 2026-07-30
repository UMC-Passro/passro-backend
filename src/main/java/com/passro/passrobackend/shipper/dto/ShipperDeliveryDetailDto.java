package com.passro.passrobackend.shipper.dto;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.place.entity.Place;
import java.time.LocalDateTime;
import java.util.List;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ShipperDeliveryDetailDto {
    private Long id;

    private SenderInfo senderInfo;
    private ShipperInfo shipperInfo;

    private Place originPlace;
    private Place destPlace;

    private DeliveryState deliveryState;
    private String memo;

    private List<DeliveryLogInfo> deliveryTimeLine;

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

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeliveryLogInfo {
        private Long id;
        private DeliveryLogType type;
        private String image;
        private LocalDateTime createdAt;

        public static DeliveryLogInfo fromEntity(DeliveryLog log) {
            if (log == null) {
                return null;
            }
            return DeliveryLogInfo.builder()
                    .id(log.getId())
                    .type(log.getType())
                    .image(log.getImage())
                    .createdAt(log.getCreatedAt())
                    .build();
        }
    }

    public static ShipperDeliveryDetailDto fromDelivery(Delivery delivery, List<DeliveryLog> logs) {
        return ShipperDeliveryDetailDto.builder()
                .id(delivery.getId())
                .senderInfo(SenderInfo.fromAccount(delivery.getSender()))
                .shipperInfo(ShipperInfo.fromAccount(delivery.getShipper()))
                .originPlace(delivery.getOrigin())
                .destPlace(delivery.getDest())
                .memo(delivery.getMemo())
                .deliveryState(delivery.getStatus())
                .deliveryTimeLine(logs != null ? logs.stream().map(DeliveryLogInfo::fromEntity).toList() : List.of())
                .build();
    }
}
