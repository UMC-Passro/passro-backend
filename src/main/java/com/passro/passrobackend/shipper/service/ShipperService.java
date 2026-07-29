package com.passro.passrobackend.shipper.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryLog;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.event.DeliveryLogEvent;
import com.passro.passrobackend.delivery.exception.DeliveryException;
import com.passro.passrobackend.delivery.exception.code.DeliveryErrorCode;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.shipper.dto.ShipperDeliveryDetailDto;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShipperService {

    private final DeliveryRepository deliveryRepository;
    private final DeliveryLogRepository deliveryLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public List<Delivery> listAllByShipper(Account account) {
        return deliveryRepository.findAllByShipper(account);
    }

    public ShipperDeliveryDetailDto getDeliveryById(Account shipper, Long id) {
        Delivery delivery = getDelivery(id);
        validateAssignedShipper(delivery, shipper);
        List<DeliveryLog> logs = deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(delivery);
        return ShipperDeliveryDetailDto.fromDelivery(delivery, logs);
    }

    public List<Delivery> listMatchRequested() {
        return deliveryRepository.findAllByStatus(DeliveryState.WAIT);
    }

    @Transactional
    public void matchAccept(Account shipper, Long id) {
        Delivery delivery = getDeliveryForUpdate(id);
        validateStatus(delivery, DeliveryState.WAIT);
        if (delivery.getShipper() != null || Boolean.TRUE.equals(delivery.getMatched())) {
            throw new DeliveryException(DeliveryErrorCode.INVALID_STATUS_TRANSITION);
        }

        // 매칭된 상태로 변경
        delivery.setShipper(shipper);
        delivery.setStatus(DeliveryState.MATCHED);
        delivery.setMatched(true);
        deliveryRepository.save(delivery);
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.MATCHED));
    }

    @Transactional
    public void acquireAccept(Account shipper, Long id) {
        Delivery delivery = getDeliveryForUpdate(id);
        validateAssignedShipper(delivery, shipper);
        validateStatus(delivery, DeliveryState.MATCHED);

        delivery.setStatus(DeliveryState.DELIVERING);
        deliveryRepository.save(delivery);
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.PICKED_UP));
    }

    @Transactional
    public void acquireConfirm(Account shipper, Long id) {
        Delivery delivery = getDeliveryForUpdate(id);
        validateAssignedShipper(delivery, shipper);
        validateStatus(delivery, DeliveryState.DELIVERING);

        delivery.setStatus(DeliveryState.CONFIRM_REQUESTED);
        deliveryRepository.save(delivery);
        eventPublisher.publishEvent(new DeliveryLogEvent(delivery, DeliveryLogType.DELIVERED));
    }

    private Delivery getDelivery(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
    }

    private Delivery getDeliveryForUpdate(Long id) {
        return deliveryRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new DeliveryException(DeliveryErrorCode.NOT_FOUND));
    }

    private void validateAssignedShipper(Delivery delivery, Account shipper) {
        if (delivery.getShipper() == null || shipper == null
                || !delivery.getShipper().getId().equals(shipper.getId())) {
            throw new DeliveryException(DeliveryErrorCode.FORBIDDEN_ACCESS);
        }
    }

    private void validateStatus(Delivery delivery, DeliveryState expected) {
        if (delivery.getStatus() != expected) {
            throw new DeliveryException(DeliveryErrorCode.INVALID_STATUS_TRANSITION);
        }
    }
}
