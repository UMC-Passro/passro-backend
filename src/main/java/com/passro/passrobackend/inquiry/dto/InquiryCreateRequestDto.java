package com.passro.passrobackend.inquiry.dto;

import com.passro.passrobackend.inquiry.enums.InquiryCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "배송 ID는 필수입니다.")
    private Long deliveryId;

    // TODO(#2): 카테고리 값 확정 시 InquiryCategory와 함께 검토
    @NotNull(message = "카테고리는 필수입니다.")
    private InquiryCategory category;

    // 제목은 선택 입력 (ERD상 nullable)
    @Size(max = 255, message = "제목은 255자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}