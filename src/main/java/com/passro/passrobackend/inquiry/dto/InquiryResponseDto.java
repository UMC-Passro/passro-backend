package com.passro.passrobackend.inquiry.dto;

import com.passro.passrobackend.inquiry.entity.Inquiry;
import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryResponseDto {
    private Long inquiryId;
    private Long deliveryId;
    private InquiryCategory category;
    private String title;
    private String content;
    private String writerNickname;
    private LocalDateTime createdAt;

    public static InquiryResponseDto fromInquiry(Inquiry inquiry) {
        return InquiryResponseDto.builder()
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
