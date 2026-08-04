package com.passro.passrobackend.shipper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.shipper.dto.ShipperDeliveryListDto;
import com.passro.passrobackend.shipper.service.ShipperService;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShipperServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @InjectMocks
    private ShipperService shipperService;

    @Test
    void listAllByShipperWithoutStatusReturnsAllAssignedDeliveries() {
        Account shipper = Account.builder().id(1L).build();
        Delivery delivery = Delivery.builder().id(10L).shipper(shipper).build();
        given(deliveryRepository.findAllByShipper(shipper)).willReturn(List.of(delivery));

        assertThat(shipperService.listAllByShipper(shipper, null)).containsExactly(delivery);
    }

    @Test
    void listAllByShipperWithStatusReturnsOnlyFilteredDeliveries() {
        Account shipper = Account.builder().id(1L).build();
        Delivery delivering = Delivery.builder()
                .id(10L)
                .shipper(shipper)
                .status(DeliveryState.DELIVERING)
                .build();
        given(deliveryRepository.findAllByShipperAndStatus(shipper, DeliveryState.DELIVERING))
                .willReturn(List.of(delivering));

        assertThat(shipperService.listAllByShipper(shipper, DeliveryState.DELIVERING))
                .containsExactly(delivering);
    }

    @Test
    void deliveryListDtoContainsCreatedAtAndEstimatedTime() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 3, 11, 0);
        Delivery delivery = Delivery.builder()
                .id(10L)
                .status(DeliveryState.WAIT)
                .createdAt(createdAt)
                .build();

        ShipperDeliveryListDto result = ShipperDeliveryListDto.fromDelivery(delivery, 30);

        assertThat(result.getCreatedAt()).isEqualTo(createdAt);
        assertThat(result.getEstimatedTimeMinutes()).isEqualTo(30);
    }
}
