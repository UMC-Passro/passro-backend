package com.passro.passrobackend.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChatMessageRequestDto(
        @NotBlank(message = "메시지 내용은 비어있을 수 없습니다.")
        @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다.")
        String content
) {
}