package com.passro.passrobackend.delivery.event;

import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DeliveryLogEvent {
    private final Delivery delivery;
    private final DeliveryLogType type;
}
