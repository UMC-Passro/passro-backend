package com.passro.passrobackend.sender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryPoint;
import com.passro.passrobackend.delivery.enums.DeliveryLogType;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.repository.DeliveryLogRepository;
import com.passro.passrobackend.delivery.repository.DeliveryPointRepository;
import com.passro.passrobackend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SenderDeliveryIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private DeliveryPointRepository deliveryPointRepository;

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    // @Test // TODO: 출발지/도착지 로직 수정 완료 후 주석 해제
    void senderCanCreateQueryPriceAgreeTermsAndCancelDelivery() throws Exception {
        Account sender = createAccount("sender");
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Laptop", "Seoul", "Busan");

        mockMvc.perform(get("/sender").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].deliveryId").value(deliveryId))
                .andExpect(jsonPath("$.result[0].goodName").value("Laptop"));

        mockMvc.perform(get("/sender/{deliveryId}", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(deliveryId))
                .andExpect(jsonPath("$.result.status").value("WAIT"))
                .andExpect(jsonPath("$.result.deliveryTimeLine[0].type").value("SEND_REQUEST"));

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        deliveryPointRepository.saveAndFlush(DeliveryPoint.builder()
                .delivery(delivery)
                .base_point(1000L)
                .distance_point(2000L)
                .weight_point(500L)
                .build());

        mockMvc.perform(get("/sender/{deliveryId}/payment", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.totalPoint").value(3500));

        mockMvc.perform(patch("/sender/{deliveryId}/terms", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        assertThat(deliveryRepository.findById(deliveryId).orElseThrow().getTerms()).isTrue();

        mockMvc.perform(patch("/sender/{deliveryId}/cancel", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        Delivery canceled = deliveryRepository.findById(deliveryId).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(DeliveryState.CANCEL);
        assertThat(deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(canceled))
                .extracting(log -> log.getType())
                .containsExactly(DeliveryLogType.SEND_REQUEST, DeliveryLogType.CANCELED);
    }

    // @Test // TODO: 출발지/도착지 로직 수정 완료 후 주석 해제
    void anotherSenderCannotReadOrModifyDelivery() throws Exception {
        Account owner = createAccount("owner");
        Account stranger = createAccount("stranger");
        long deliveryId = createDelivery(accessToken(owner), "Phone", "A", "B");
        String strangerToken = accessToken(stranger);

        mockMvc.perform(get("/sender/{deliveryId}", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("DELIVERY403_1"));

        mockMvc.perform(patch("/sender/{deliveryId}/terms", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden());
    }

    // @Test // TODO: 출발지/도착지 로직 수정 완료 후 주석 해제
    void matchedDeliveryCannotBeCanceled() throws Exception {
        Account sender = createAccount("matched-owner");
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Camera", "A", "B");
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        delivery.setStatus(DeliveryState.MATCHED);
        deliveryRepository.saveAndFlush(delivery);

        mockMvc.perform(patch("/sender/{deliveryId}/cancel", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_1"));
    }

    // @Test // TODO: 출발지/도착지 로직 수정 완료 후 주석 해제
    void deliveryCannotBeCompletedBeforeConfirmationRequest() throws Exception {
        Account sender = createAccount("early-complete");
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Bag", "A", "B");

        mockMvc.perform(patch("/sender/{deliveryId}/complete", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_2"));
    }
}
