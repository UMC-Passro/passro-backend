package com.passro.passrobackend.inquiry.dto;

import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InquiryCreateRequestDto {
    private Long deliveryId;

    // TODO(#2): 카테고리 값 확정 시 InquiryCategory와 함께 검토
    private InquiryCategory category;

    private String title;

    private String content;
}
