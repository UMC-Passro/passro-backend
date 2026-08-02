package com.passro.passrobackend.deliveryinquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.deliveryinquiry.dto.DeliveryInquiryCreateRequestDto;
import com.passro.passrobackend.deliveryinquiry.dto.DeliveryInquiryResponseDto;
import com.passro.passrobackend.deliveryinquiry.entity.DeliveryInquiry;
import com.passro.passrobackend.deliveryinquiry.repository.DeliveryInquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryInquiryService {

    private final DeliveryInquiryRepository deliveryInquiryRepository;
    private final DeliveryRepository deliveryRepository;

    // 배송 문의 작성
    @Transactional
    public DeliveryInquiryResponseDto createDeliveryInquiry(Account account, DeliveryInquiryCreateRequestDto request) {
        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        DeliveryInquiry inquiry = DeliveryInquiry.builder()
                .delivery(delivery)
                .account(account)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return DeliveryInquiryResponseDto.fromDeliveryInquiry(deliveryInquiryRepository.save(inquiry));
    }

    // 특정 배송에 달린 문의 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<DeliveryInquiryResponseDto> getDeliveryInquiriesByDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        return deliveryInquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery)
                .stream()
                .map(DeliveryInquiryResponseDto::fromDeliveryInquiry)
                .toList();
    }
}
