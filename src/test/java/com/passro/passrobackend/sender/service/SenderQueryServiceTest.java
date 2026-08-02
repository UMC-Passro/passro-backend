package com.passro.passrobackend.sender.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryGoodInfo;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.entity.DeliveryPoint;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.sender.dto.SenderDeliveryDetailDto;
import com.passro.passrobackend.sender.dto.SenderDeliveryListDto;
import com.passro.passrobackend.sender.dto.SenderPaymentAmountDto;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SenderQueryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private DeliveryLogRepository deliveryLogRepository;

    @Mock
    private SenderDeliveryValidator senderDeliveryValidator;

    @InjectMocks
    private SenderQueryService senderQueryService;

    @Test
    @DisplayName("발송자 배송 목록 조회 시 Place 객체 및 물품명이 정상 포함되어 반환된다")
    void getSenders_success() {
        // Given
        Account sender = Account.builder().id(1L).nickname("sender").build();
        Place origin = Place.builder().id(10L).subwayRouteName("2호선").subwayStationName("강남").build();
        Place dest = Place.builder().id(20L).subwayRouteName("2호선").subwayStationName("홍대입구").build();
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 3, 10, 30);

        Delivery delivery = Delivery.builder()
                .id(100L)
                .sender(sender)
                .origin(origin)
                .dest(dest)
                .status(DeliveryState.WAIT)
                .createdAt(createdAt)
                .build();
        delivery.attachGoodInfo(DeliveryGoodInfo.builder().name("노트북").build());

        given(deliveryRepository.findAllBySender(sender)).willReturn(List.of(delivery));

        // When
        List<SenderDeliveryListDto> result = senderQueryService.getSenders(sender, null);

        // Then
        assertThat(result).hasSize(1);
        SenderDeliveryListDto dto = result.get(0);
        assertThat(dto.getDeliveryId()).isEqualTo(100L);
        assertThat(dto.getName()).isEqualTo("노트북");
        assertThat(dto.getOriginPlace()).isEqualTo(origin);
        assertThat(dto.getDestPlace()).isEqualTo(dest);
        assertThat(dto.getStatus()).isEqualTo(DeliveryState.WAIT);
        assertThat(dto.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("발송자 배송 목록이 없으면 빈 리스트를 반환한다")
    void getSenders_empty() {
        // Given
        Account sender = Account.builder().id(1L).build();
        given(deliveryRepository.findAllBySender(sender)).willReturn(List.of());

        // When
        List<SenderDeliveryListDto> result = senderQueryService.getSenders(sender, null);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("배송 상태 필터가 있으면 발송자의 해당 상태 배송만 조회한다")
    void getSenders_withStatusFilter() {
        Account sender = Account.builder().id(1L).build();
        Delivery delivered = Delivery.builder()
                .id(100L)
                .sender(sender)
                .status(DeliveryState.DELIVERED)
                .build();
        given(deliveryRepository.findAllBySenderAndStatus(sender, DeliveryState.DELIVERED))
                .willReturn(List.of(delivered));

        List<SenderDeliveryListDto> result = senderQueryService.getSenders(
                sender, DeliveryState.DELIVERED);

        assertThat(result).singleElement()
                .extracting(SenderDeliveryListDto::getStatus)
                .isEqualTo(DeliveryState.DELIVERED);
    }

    @Test
    @DisplayName("발송 배송 단건 상세 정보 조회 성공")
    void getDeliveryDetail_success() {
        // Given
        Account sender = Account.builder().id(1L).build();
        Account shipper = Account.builder().id(2L).name("배송기사").picture("profile.jpg").build();
        Place origin = Place.builder().id(10L).subwayRouteName("2호선").subwayStationName("강남").build();
        Place destination = Place.builder().id(20L).subwayRouteName("신분당선").subwayStationName("판교").build();
        Delivery delivery = Delivery.builder()
                .id(100L)
                .sender(sender)
                .shipper(shipper)
                .origin(origin)
                .dest(destination)
                .status(DeliveryState.DELIVERING)
                .build();
        delivery.attachGoodInfo(DeliveryGoodInfo.builder().name("노트북").build());

        DeliveryLog log1 = DeliveryLog.builder().id(1L).type(DeliveryLogType.SEND_REQUEST).createdAt(LocalDateTime.now()).build();
        DeliveryLog log2 = DeliveryLog.builder().id(2L).type(DeliveryLogType.MATCHED).createdAt(LocalDateTime.now()).build();

        given(senderDeliveryValidator.getDeliveryAndValidateOwnership(100L, sender)).willReturn(delivery);
        given(deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(delivery)).willReturn(List.of(log1, log2));

        // When
        SenderDeliveryDetailDto result = senderQueryService.getDeliveryDetail(sender, 100L);

        // Then
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getName()).isEqualTo("노트북");
        assertThat(result.getOriginPlace()).isEqualTo(origin);
        assertThat(result.getDestPlace()).isEqualTo(destination);
        assertThat(result.getStatus()).isEqualTo(DeliveryState.DELIVERING);
        assertThat(result.getShipperInfo().getName()).isEqualTo("배송기사");
        assertThat(result.getDeliveryTimeLine()).hasSize(2);
    }

    @Test
    @DisplayName("발송 금액 정보 조회 성공")
    void getPaymentAmount_success() {
        // Given
        Account sender = Account.builder().id(1L).build();
        Delivery delivery = Delivery.builder().id(100L).sender(sender).build();
        DeliveryPoint point = DeliveryPoint.builder()
                .id(10L)
                .base_point(1000L)
                .distance_point(500L)
                .weight_point(300L)
                .build();
        delivery.attachPoint(point);

        given(senderDeliveryValidator.getDeliveryAndValidateOwnership(100L, sender)).willReturn(delivery);

        // When
        SenderPaymentAmountDto result = senderQueryService.getPaymentAmount(sender, 100L);

        // Then
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getBasePoint()).isEqualTo(1000L);
        assertThat(result.getDistancePoint()).isEqualTo(500L);
        assertThat(result.getWeightPoint()).isEqualTo(300L);
        assertThat(result.getTotalPoint()).isEqualTo(1800L);
    }

    @Test
    @DisplayName("발송 금액 정보가 없으면 DELIVERY_POINT_NOT_FOUND 예외가 발생한다")
    void getPaymentAmount_pointNotFound() {
        // Given
        Account sender = Account.builder().id(1L).build();
        Delivery delivery = Delivery.builder().id(100L).sender(sender).build();

        given(senderDeliveryValidator.getDeliveryAndValidateOwnership(100L, sender)).willReturn(delivery);

        // When & Then
        assertThatThrownBy(() -> senderQueryService.getPaymentAmount(sender, 100L))
                .isInstanceOf(DeliveryException.class)
                .extracting(e -> ((DeliveryException) e).getCode())
                .isEqualTo(DeliveryErrorCode.DELIVERY_POINT_NOT_FOUND);
    }
}
