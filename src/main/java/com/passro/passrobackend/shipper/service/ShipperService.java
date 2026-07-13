package com.passro.passrobackend.shipper.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryGoodInfoRepository;
import com.passro.passrobackend.delivery.repository.DeliveryInfoRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShipperService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryInfoRepository deliveryInfoRepository;
    private final DeliveryGoodInfoRepository deliveryGoodInfoRepository;

    public List<Delivery> listAllBySender(Account account) {
        return deliveryRepository.findAllBySender(account);
    }

    public Delivery getDeliveryById(Account shipper, Long id){
        return deliveryRepository.findById(id).orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
    }

    public List<Delivery> listMatchRequested(){
        return deliveryRepository.findAllByStatus(DeliveryState.WAIT);
    }

    public void matchAccept(Account shipper, Long id){
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
        delivery.setShipper(shipper);
        delivery.setStatus(DeliveryState.MATCHED);
        deliveryRepository.save(delivery);
    }

    public void acquireAccept(Account shipper, Long id){
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
        delivery.setStatus(DeliveryState.DELIVERING);
        deliveryRepository.save(delivery);
    }

    public void acquireConfirm(Account shipper, Long id){
        Delivery delivery = deliveryRepository.findById(id).orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
        delivery.setStatus(DeliveryState.CONFIRM_REQUESTED);
        deliveryRepository.save(delivery);
    }
}
