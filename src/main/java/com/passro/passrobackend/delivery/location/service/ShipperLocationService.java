package com.passro.passrobackend.delivery.location.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.location.dto.ShipperLocationResponseDto;
import com.passro.passrobackend.delivery.location.dto.ShipperLocationUpdateRequestDto;
import com.passro.passrobackend.delivery.location.exception.ShipperLocationException;
import com.passro.passrobackend.delivery.location.exception.code.ShipperLocationErrorCode;
import com.passro.passrobackend.delivery.location.model.ShipperLocation;
import com.passro.passrobackend.delivery.location.repository.ShipperLocationRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.sender.service.SenderDeliveryValidator;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipperLocationService {

    private final DeliveryRepository deliveryRepository;
    private final PlaceRepository placeRepository;
    private final SenderDeliveryValidator senderDeliveryValidator;
    private final ShipperLocationRepository shipperLocationRepository;

    public ShipperLocationResponseDto updateLocation(
            Account shipper,
            ShipperLocationUpdateRequestDto request) {
        if (!deliveryRepository.existsByShipperAndStatus(shipper, DeliveryState.DELIVERING)) {
            throw new ShipperLocationException(ShipperLocationErrorCode.UPDATE_NOT_ALLOWED);
        }
        if (!placeRepository.existsById(request.placeId())) {
            throw new DeliveryException(DeliveryErrorCode.PLACE_NOT_FOUND);
        }

        ShipperLocation location = new ShipperLocation(
                request.latitude(),
                request.longitude(),
                request.placeId(),
                LocalDateTime.now());
        shipperLocationRepository.save(shipper.getId(), location);
        return ShipperLocationResponseDto.from(location);
    }

    public ShipperLocationResponseDto getLocation(Account sender, Long deliveryId) {
        Delivery delivery = senderDeliveryValidator.getDeliveryAndValidateOwnership(deliveryId, sender);
        if (delivery.getStatus() != DeliveryState.DELIVERING || delivery.getShipper() == null) {
            throw new ShipperLocationException(ShipperLocationErrorCode.TRACKING_NOT_AVAILABLE);
        }

        ShipperLocation location = shipperLocationRepository.findByShipperId(delivery.getShipper().getId())
                .orElseThrow(() -> new ShipperLocationException(ShipperLocationErrorCode.NOT_FOUND));
        return ShipperLocationResponseDto.from(location);
    }
}
