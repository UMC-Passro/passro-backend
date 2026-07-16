package com.passro.passrobackend.inquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.inquiry.dto.InquiryCreateRequestDto;
import com.passro.passrobackend.inquiry.dto.InquiryResponseDto;
import com.passro.passrobackend.inquiry.entity.Inquiry;
import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import com.passro.passrobackend.inquiry.repository.InquiryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private InquiryService inquiryService;

    private Account account(Long id) {
        return Account.builder().id(id).nickname("tester").build();
    }

    private Delivery delivery(Long id) {
        return Delivery.builder().id(id).build();
    }

    @Test
    @DisplayName("문의 작성 성공")
    void createInquiry_success() {
        // given
        Account account = account(10L);
        InquiryCreateRequestDto request = InquiryCreateRequestDto.builder()
                .deliveryId(1L)
                .category(InquiryCategory.DELAY)
                .title("제목")
                .content("내용")
                .build();
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery(1L)));
        given(inquiryRepository.save(any(Inquiry.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        InquiryResponseDto response = inquiryService.createInquiry(account, request);

        // then
        assertThat(response.getDeliveryId()).isEqualTo(1L);
        assertThat(response.getCategory()).isEqualTo(InquiryCategory.DELAY);
        assertThat(response.getContent()).isEqualTo("내용");
        assertThat(response.getWriterNickname()).isEqualTo("tester");
        verify(inquiryRepository).save(any(Inquiry.class));
    }

    @Test
    @DisplayName("문의 작성 실패 - 존재하지 않는 배송")
    void createInquiry_deliveryNotFound() {
        // given
        InquiryCreateRequestDto request = InquiryCreateRequestDto.builder()
                .deliveryId(999L)
                .category(InquiryCategory.ETC)
                .content("내용")
                .build();
        given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.createInquiry(account(10L), request))
                .isInstanceOf(DeliveryException.class);
        verify(inquiryRepository, never()).save(any());
    }

    @Test
    @DisplayName("문의 조회 성공 - 배송별 목록 (최신순)")
    void getInquiries_success() {
        // given
        Delivery delivery = delivery(1L);
        Inquiry inquiry = Inquiry.builder()
                .id(100L)
                .delivery(delivery)
                .account(account(10L))
                .category(InquiryCategory.DAMAGE)
                .content("파손됐어요")
                .build();
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
        given(inquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery))
                .willReturn(List.of(inquiry));

        // when
        List<InquiryResponseDto> result = inquiryService.getInquiriesByDelivery(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInquiryId()).isEqualTo(100L);
        assertThat(result.get(0).getCategory()).isEqualTo(InquiryCategory.DAMAGE);
    }

    @Test
    @DisplayName("문의 조회 성공 - 문의가 없으면 빈 목록")
    void getInquiries_empty() {
        // given
        Delivery delivery = delivery(1L);
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
        given(inquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery))
                .willReturn(List.of());

        // when
        List<InquiryResponseDto> result = inquiryService.getInquiriesByDelivery(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("문의 조회 실패 - 존재하지 않는 배송")
    void getInquiries_deliveryNotFound() {
        // given
        given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> inquiryService.getInquiriesByDelivery(999L))
                .isInstanceOf(DeliveryException.class);
    }
}
