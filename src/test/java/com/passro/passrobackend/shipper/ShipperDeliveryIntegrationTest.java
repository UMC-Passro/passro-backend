package com.passro.passrobackend.shipper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class ShipperDeliveryIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    @Test
    void shipperCanProgressAssignedDeliveryThroughEveryState() throws Exception {
        Account sender = createAccount("flow-sender");
        Account shipper = createAccount("flow-shipper");
        String senderToken = accessToken(sender);
        String shipperToken = accessToken(shipper);
        long deliveryId = createDelivery(senderToken, "Monitor", "Seoul", "Daejeon");

        mockMvc.perform(get("/shipper/matched").header("Authorization", bearer(shipperToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(deliveryId));

        patchAsShipper(deliveryId, "matched", shipperToken);
        assertState(deliveryId, DeliveryState.MATCHED, shipper.getId());

        mockMvc.perform(get("/shipper/").header("Authorization", bearer(shipperToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].id").value(deliveryId));
        mockMvc.perform(get("/shipper/{deliveryId}/", deliveryId)
                        .header("Authorization", bearer(shipperToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.deliveryState").value("MATCHED"));

        patchAsShipper(deliveryId, "acquire", shipperToken);
        assertState(deliveryId, DeliveryState.DELIVERING, shipper.getId());

        patchAsShipper(deliveryId, "confirm", shipperToken);
        assertState(deliveryId, DeliveryState.CONFIRM_REQUESTED, shipper.getId());

        mockMvc.perform(patch("/sender/{deliveryId}/complete", deliveryId)
                        .header("Authorization", bearer(senderToken)))
                .andExpect(status().isOk());
        assertState(deliveryId, DeliveryState.DELIVERED, shipper.getId());

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        assertThat(deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(delivery))
                .extracting(log -> log.getType())
                .containsExactly(
                        DeliveryLogType.SEND_REQUEST,
                        DeliveryLogType.MATCHED,
                        DeliveryLogType.PICKED_UP,
                        DeliveryLogType.DELIVERED,
                        DeliveryLogType.DONE
                );
    }

    @Test
    void shipperCannotSkipDeliveryStates() throws Exception {
        Account sender = createAccount("state-sender");
        Account shipper = createAccount("state-shipper");
        long deliveryId = createDelivery(accessToken(sender), "Keyboard", "A", "B");
        String shipperToken = accessToken(shipper);

        patchAsShipper(deliveryId, "matched", shipperToken);

        mockMvc.perform(patch("/shipper/{deliveryId}/confirm", deliveryId)
                        .header("Authorization", bearer(shipperToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_3"));

        patchAsShipper(deliveryId, "acquire", shipperToken);
        mockMvc.perform(patch("/shipper/{deliveryId}/matched", deliveryId)
                        .header("Authorization", bearer(shipperToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_3"));
    }

    @Test
    void unassignedShipperCannotReadOrProgressAnotherShippersDelivery() throws Exception {
        Account sender = createAccount("owner-sender");
        Account assigned = createAccount("assigned-shipper");
        Account stranger = createAccount("stranger-shipper");
        long deliveryId = createDelivery(accessToken(sender), "Tablet", "A", "B");
        patchAsShipper(deliveryId, "matched", accessToken(assigned));
        String strangerToken = accessToken(stranger);

        mockMvc.perform(get("/shipper/{deliveryId}/", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("DELIVERY403_1"));

        mockMvc.perform(patch("/shipper/{deliveryId}/acquire", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("DELIVERY403_1"));

        mockMvc.perform(patch("/shipper/{deliveryId}/matched", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_3"));
    }

    private void patchAsShipper(long deliveryId, String action, String token) throws Exception {
        mockMvc.perform(patch("/shipper/{deliveryId}/{action}", deliveryId, action)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
    }

    private void assertState(long deliveryId, DeliveryState expected, Long shipperId) {
        entityManager.flush();
        entityManager.clear();
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        assertThat(delivery.getStatus()).isEqualTo(expected);
        assertThat(delivery.getShipper().getId()).isEqualTo(shipperId);
    }
}
