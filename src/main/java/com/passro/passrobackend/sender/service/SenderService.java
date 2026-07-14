package com.passro.passrobackend.sender.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.entity.DeliveryPoint;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.event.DeliveryLogEvent;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.delivery.repository.DeliveryPointRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.sender.dto.SenderDeliveryDetailDto;
import com.passro.passrobackend.sender.dto.SenderPaymentAmountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SenderService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final DeliveryPointRepository deliveryPointRepository;

    private final ApplicationEventPublisher eventPublisher;

    // 발송 단건 상세 정보 조회
    @Transactional(readOnly = true)
    public SenderDeliveryDetailDto getDeliveryDetail(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        // 배송 타임라인을 날짜 오름차순으로 조회
        List<DeliveryLog> logs = deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(delivery);

        return SenderDeliveryDetailDto.fromEntity(delivery, logs);
    }

    // 발송 금액 정보 조회
    @Transactional(readOnly = true)
    public SenderPaymentAmountDto getPaymentAmount(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        DeliveryPoint deliveryPoint = deliveryPointRepository.findByDelivery(delivery)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        return SenderPaymentAmountDto.fromEntity(deliveryPoint);
    }

    // 발송 완료 처리
    @Transactional
    public void completeDelivery(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        delivery.setStatus(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        // 배송 프로세스 최종 완료 처리 로그에 저장
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.DONE));
    }

    // 발송 약관 동의 변경 후 저장
    @Transactional
    public void agreeTerms(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        delivery.setTerms(true);
        deliveryRepository.save(delivery);
    }

    // 발송 요청 취소 처리
    @Transactional
    public void cancelDelivery(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        // 매칭이 되었다면, 취소 할 수 없음.
        if (delivery.getStatus() != DeliveryState.WAIT) {
            throw new DeliveryException(DeliveryErrorCode.CANNOT_CANCEL);
        }

        delivery.setStatus(DeliveryState.CANCEL);
        deliveryRepository.save(delivery);

        // 배송 취소 처리 내역 로그에 저장
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.CANCELED));
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
