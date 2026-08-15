package com.passro.passrobackend.chat.dto;

import com.passro.passrobackend.chat.entity.ChatMessage;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(types = "object", description = "채팅 메시지 응답")
public record ChatMessageResponseDto(
        Long id,
        Long senderId,
        String senderNickname,
        String content,
        boolean isRead,
        LocalDateTime createdAt,
        @Schema(description = "첨부 이미지 S3 키", nullable = true)
        String imageKey
) {
    public ChatMessageResponseDto(
            Long id,
            Long senderId,
            String senderNickname,
            String content,
            boolean isRead,
            LocalDateTime createdAt
    ) {
        this(id, senderId, senderNickname, content, isRead, createdAt, null);
    }

    public static ChatMessageResponseDto from(ChatMessage message) {
        return new ChatMessageResponseDto(
                message.getId(),
                message.getSender().getId(),
                message.getSender().getNickname(),
                message.getContent(),
                message.isRead(),
                message.getCreatedAt(),
                message.getImageKey()
        );
    }
}
