package com.passro.passrobackend.chat.dto;

import com.passro.passrobackend.delivery.enums.DeliveryState;

public record ChatRoomInfoResponseDto(
        String partnerNickname,
        String partnerPicture,
        String itemName,
        String departure,
        String arrival,
        DeliveryState deliveryStatus
) {
}