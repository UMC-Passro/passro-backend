package com.passro.passrobackend.deliveryinquiry.dto;

import com.passro.passrobackend.deliveryinquiry.entity.DeliveryInquiry;
import com.passro.passrobackend.deliveryinquiry.enums.DeliveryInquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryInquiryResponseDto {
    private Long inquiryId;
    private Long deliveryId;
    private DeliveryInquiryCategory category;
    private String title;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;

    public static DeliveryInquiryResponseDto fromDeliveryInquiry(DeliveryInquiry inquiry) {
        return DeliveryInquiryResponseDto.builder()
                .inquiryId(inquiry.getId())
                .deliveryId(inquiry.getDelivery() != null ? inquiry.getDelivery().getId() : null)
                .category(inquiry.getCategory())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .writerNickname(inquiry.getAccount() != null ? inquiry.getAccount().getNickname() : null)
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
