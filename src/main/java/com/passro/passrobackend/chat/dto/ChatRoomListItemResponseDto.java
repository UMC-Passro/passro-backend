package com.passro.passrobackend.chat.dto;

import java.time.LocalDateTime;

public record ChatRoomListItemResponseDto(
        Long deliveryId,
        ChatPartnerDto partner,
        String itemName,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount
) {
}