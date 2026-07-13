package com.passro.passrobackend.sender.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.repository.DeliveryGoodInfoRepository;
import com.passro.passrobackend.delivery.repository.DeliveryInfoRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SenderService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final DeliveryGoodInfoRepository deliveryGoodInfoRepository;
}
