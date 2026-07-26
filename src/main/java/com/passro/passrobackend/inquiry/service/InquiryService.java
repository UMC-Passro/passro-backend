package com.passro.passrobackend.inquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.entity.Inquiry;
import com.passro.passrobackend.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final DeliveryRepository deliveryRepository;

    // 문의 작성
    @Transactional
    public InquiryResponseDto createInquiry(Account account, InquiryCreateRequestDto request) {
        Delivery delivery = deliveryRepository.findById(request.getDeliveryId())
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        Inquiry inquiry = Inquiry.builder()
                .delivery(delivery)
                .account(account)
                .category(request.getCategory())
                .title(request.getTitle())
                .content(request.getContent())
                .build();

        return InquiryResponseDto.fromInquiry(inquiryRepository.save(inquiry));
    }

    // 특정 배송에 달린 문의 목록 조회 (최신순)
    @Transactional(readOnly = true)
    public List<InquiryResponseDto> getInquiriesByDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));

        return inquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery)
                .stream()
                .map(InquiryResponseDto::fromInquiry)
                .toList();
    }
}
