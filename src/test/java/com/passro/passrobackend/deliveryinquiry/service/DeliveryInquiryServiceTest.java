package com.passro.passrobackend.deliveryinquiry.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.deliveryinquiry.dto.DeliveryInquiryCreateRequestDto;
import com.passro.passrobackend.deliveryinquiry.dto.DeliveryInquiryResponseDto;
import com.passro.passrobackend.deliveryinquiry.entity.DeliveryInquiry;
import com.passro.passrobackend.deliveryinquiry.enums.DeliveryInquiryCategory;
import com.passro.passrobackend.deliveryinquiry.repository.DeliveryInquiryRepository;
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
class DeliveryInquiryServiceTest {

    @Mock
    private DeliveryInquiryRepository deliveryInquiryRepository;

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private DeliveryInquiryService deliveryInquiryService;

    private Account account(Long id) {
        return Account.builder().id(id).nickname("tester").build();
    }

    private Delivery delivery(Long id) {
        return Delivery.builder().id(id).build();
    }

    @Test
    @DisplayName("배송 문의 작성 성공")
    void createDeliveryInquiry_success() {
        // given
        Account account = account(10L);
        DeliveryInquiryCreateRequestDto request = DeliveryInquiryCreateRequestDto.builder()
                .deliveryId(1L)
                .category(DeliveryInquiryCategory.DELAY)
                .title("제목")
                .content("내용")
                .build();
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery(1L)));
        given(deliveryInquiryRepository.save(any(DeliveryInquiry.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        DeliveryInquiryResponseDto response = deliveryInquiryService.createDeliveryInquiry(account, request);

        // then
        assertThat(response.getDeliveryId()).isEqualTo(1L);
        assertThat(response.getCategory()).isEqualTo(DeliveryInquiryCategory.DELAY);
        assertThat(response.getContent()).isEqualTo("내용");
        assertThat(response.getWriterNickname()).isEqualTo("tester");
        verify(deliveryInquiryRepository).save(any(DeliveryInquiry.class));
    }

    @Test
    @DisplayName("배송 문의 작성 실패 - 존재하지 않는 배송")
    void createDeliveryInquiry_deliveryNotFound() {
        // given
        DeliveryInquiryCreateRequestDto request = DeliveryInquiryCreateRequestDto.builder()
                .deliveryId(999L)
                .category(DeliveryInquiryCategory.ETC)
                .content("내용")
                .build();
        given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryInquiryService.createDeliveryInquiry(account(10L), request))
                .isInstanceOf(DeliveryException.class);
        verify(deliveryInquiryRepository, never()).save(any());
    }

    @Test
    @DisplayName("배송 문의 조회 성공 - 배송별 목록 (최신순)")
    void getDeliveryInquiries_success() {
        // given
        Delivery delivery = delivery(1L);
        DeliveryInquiry inquiry = DeliveryInquiry.builder()
                .id(100L)
                .delivery(delivery)
                .account(account(10L))
                .category(DeliveryInquiryCategory.DAMAGE)
                .content("파손됐어요")
                .build();
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
        given(deliveryInquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery))
                .willReturn(List.of(inquiry));

        // when
        List<DeliveryInquiryResponseDto> result = deliveryInquiryService.getDeliveryInquiriesByDelivery(1L);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInquiryId()).isEqualTo(100L);
        assertThat(result.get(0).getCategory()).isEqualTo(DeliveryInquiryCategory.DAMAGE);
    }

    @Test
    @DisplayName("배송 문의 조회 성공 - 문의가 없으면 빈 목록")
    void getDeliveryInquiries_empty() {
        // given
        Delivery delivery = delivery(1L);
        given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
        given(deliveryInquiryRepository.findAllByDeliveryOrderByCreatedAtDesc(delivery))
                .willReturn(List.of());

        // when
        List<DeliveryInquiryResponseDto> result = deliveryInquiryService.getDeliveryInquiriesByDelivery(1L);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("배송 문의 조회 실패 - 존재하지 않는 배송")
    void getDeliveryInquiries_deliveryNotFound() {
        // given
        given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> deliveryInquiryService.getDeliveryInquiriesByDelivery(999L))
                .isInstanceOf(DeliveryException.class);
    }
}
