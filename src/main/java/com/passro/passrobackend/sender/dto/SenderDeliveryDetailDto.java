package com.passro.passrobackend.sender.dto;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.place.entity.Place;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SenderDeliveryDetailDto {
    private Long id;
    private DeliveryState status; // 현재 배송 상태
    private ShipperInfo shipperInfo; // 배송자 정보
    private List<DeliveryLogInfo> deliveryTimeLine; // 배송 타임라인

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
        private LocalDateTime createdAt;

        public static DeliveryLogInfo fromEntity(DeliveryLog log) {
            if (log == null) {
                return null;
            }
            return DeliveryLogInfo.builder()
                    .id(log.getId())
                    .type(log.getType())
                    .createdAt(log.getCreatedAt())
                    .build();
        }
    }

    public static SenderDeliveryDetailDto fromEntity(Delivery delivery, List<DeliveryLog> logs) {
        return SenderDeliveryDetailDto.builder()
                .id(delivery.getId())
                .status(delivery.getStatus())
                .shipperInfo(ShipperInfo.fromAccount(delivery.getShipper()))
                .deliveryTimeLine(logs != null ? logs.stream().map(DeliveryLogInfo::fromEntity).toList() : List.of())
                .build();
    }
}
