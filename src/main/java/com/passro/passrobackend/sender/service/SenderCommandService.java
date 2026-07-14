package com.passro.passrobackend.sender.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.event.DeliveryLogEvent;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.entity.DeliveryGoodInfo;
import com.passro.passrobackend.delivery.repository.DeliveryGoodInfoRepository;
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.sender.dto.SenderDeliveryCreateRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 발송 관련 DB 수정 (INSERT, UPDATE, DELETE) 관련 Service
@Service
@RequiredArgsConstructor
@Transactional
public class SenderCommandService {

    private final DeliveryRepository deliveryRepository;
    private final PlaceRepository placeRepository;
    private final DeliveryGoodInfoRepository deliveryGoodInfoRepository;

    private final ApplicationEventPublisher eventPublisher;

    // 발송 완료 처리
    public void completeDelivery(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        delivery.setStatus(DeliveryState.DELIVERED);
        deliveryRepository.save(delivery);

        // 배송 프로세스 최종 완료 처리 로그에 저장
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.DONE));
    }

    // 배송 요청 생성
    public Long createDelivery(Account sender, SenderDeliveryCreateRequestDto request) {
        // 출발지 및 도착지 Place 엔티티 생성/저장
        // TODO: 주소 정책 확정 전까지 임시 생성 로직을 사용합니다. (2026-07-14 기준)
        Place origin = Place.builder().address(request.getOriginAddress()).build();
        Place dest = Place.builder().address(request.getDestAddress()).build();
        placeRepository.save(origin);
        placeRepository.save(dest);

        // 배송 (Delivery) 엔티티 생성 및 저장
        Delivery delivery = Delivery.builder()
                .sender(sender)
                .origin(origin)
                .dest(dest)
                .memo(request.getMemo())
                .status(DeliveryState.WAIT)
                .terms(false)
                .matched(false)
                .build();
        deliveryRepository.save(delivery);

        // 배송 물품 정보 (DeliveryGoodInfo) 생성 및 저장
        DeliveryGoodInfo goodInfo = DeliveryGoodInfo.builder()
                .delivery(delivery)
                .name(request.getName())
                .price(request.getPrice())
                .size(request.getSize()) // TODO: 배송 사이즈는 enum으로 관리 고려 중입니다.
                .picture(request.getPicture())
                .build();
        deliveryGoodInfoRepository.save(goodInfo);

        // 배송 요청 로그 저장
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.SEND_REQUEST));

        // 생성된 배송 정보 id return
        return delivery.getId();
    }

    // 발송 약관 동의
    public void agreeTerms(Account sender, Long deliveryId) {
        Delivery delivery = getDeliveryOrThrow(deliveryId);
        validateSenderOwnership(delivery, sender);

        delivery.setTerms(true);
        deliveryRepository.save(delivery);
    }

    // 발송 요청 취소 처리
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
