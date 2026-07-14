package com.passro.passrobackend.sender.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.entity.DeliveryPoint;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.delivery.repository.DeliveryPointRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.delivery.entity.DeliveryGoodInfo;
import com.passro.passrobackend.delivery.repository.DeliveryGoodInfoRepository;
import com.passro.passrobackend.sender.dto.SenderDeliveryListDto;
import com.passro.passrobackend.sender.dto.SenderDeliveryDetailDto;
import com.passro.passrobackend.sender.dto.SenderPaymentAmountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// 발송 관련 DB 조회 Service
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SenderQueryService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeliveryPointRepository deliveryPointRepository;
    private final DeliveryGoodInfoRepository deliveryGoodInfoRepository;

    // 발송자 배송 목록 전체 조회
    public List<SenderDeliveryListDto> getSenders(Account sender) {
        List<Delivery> deliveries = deliveryRepository.findAllBySender(sender);

        if (deliveries.isEmpty()) {
            return List.of();
        }

        List<DeliveryGoodInfo> goodInfos = deliveryGoodInfoRepository.findByDeliveryIn(deliveries);
        Map<Long, String> goodNameMap = goodInfos.stream()
                .collect(Collectors.toMap(
                        info -> info.getDelivery().getId(),
                        DeliveryGoodInfo::getName,
                        (existing, replacement) -> existing
                ));

        return deliveries.stream().map(delivery -> {
            String goodName = goodNameMap.getOrDefault(delivery.getId(), "");

            return SenderDeliveryListDto.builder()
                    .deliveryId(delivery.getId())
                    .goodName(goodName)
                    .originAddress(delivery.getOrigin() != null ? delivery.getOrigin().getAddress() : "")
                    .destAddress(delivery.getDest() != null ? delivery.getDest().getAddress() : "")
                    .status(delivery.getStatus())
                    .build();
        }).toList();
    }

    // 발송 단건 상세 정보 조회
    public SenderDeliveryDetailDto getDeliveryDetail(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        // 배송 타임라인을 날짜 오름차순으로 조회
        List<DeliveryLog> logs = deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(delivery);

        return SenderDeliveryDetailDto.fromEntity(delivery, logs);
    }

    // 발송 금액 정보 조회
    public SenderPaymentAmountDto getPaymentAmount(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        DeliveryPoint deliveryPoint = deliveryPointRepository.findByDelivery(delivery)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        return SenderPaymentAmountDto.fromEntity(deliveryPoint);
    }

    // 배송 ID로 배송 조회 혹은 예외 발생
    private Delivery getDeliveryOrThrow(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
    }

    // 발송자 권한 검증
    private void validateSenderOwnership(Delivery delivery, Account sender) {
        if (!delivery.getSender().getId().equals(sender.getId())) {
            throw new DeliveryException(DeliveryErrorCode.FORBIDDEN_ACCESS);
        }
    }

}
