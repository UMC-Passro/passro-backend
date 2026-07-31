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
import com.passro.passrobackend.place.entity.Place;
import com.passro.passrobackend.place.repository.PlaceRepository;
import com.passro.passrobackend.point.enums.PointIncrementReason;
import com.passro.passrobackend.point.repository.PointLogRepository;
import com.passro.passrobackend.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

@Transactional
class SenderDeliveryIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private DeliveryPointRepository deliveryPointRepository;

    @Autowired
    private DeliveryLogRepository deliveryLogRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private PointLogRepository pointLogRepository;

    private long sourceId;
    private long destId;

    @BeforeEach
    void setUpPlaces() {
        List<Place> places = placeRepository.findAll();
        if (places.size() >= 2) {
            sourceId = places.get(0).getId();
            destId = places.get(1).getId();
        } else {
            Place p1 = placeRepository.saveAndFlush(Place.builder().subwayRouteName("1호선").subwayStationName("A역").build());
            Place p2 = placeRepository.saveAndFlush(Place.builder().subwayRouteName("1호선").subwayStationName("B역").build());
            sourceId = p1.getId();
            destId = p2.getId();
        }
    }

    @Test
    void senderCanCreateQueryPriceAgreeTermsAndCancelDelivery() throws Exception {
        Account sender = createAccount("sender");
        sender.setPoint(10000L);
        accountRepository.saveAndFlush(sender);
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Laptop", sourceId, destId);

        mockMvc.perform(get("/sender").header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result[0].deliveryId").value(deliveryId))
                .andExpect(jsonPath("$.result[0].name").value("Laptop"))
                .andExpect(jsonPath("$.result[0].originPlace.id").value(sourceId))
                .andExpect(jsonPath("$.result[0].destPlace.id").value(destId));

        mockMvc.perform(get("/sender/{deliveryId}", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.id").value(deliveryId))
                .andExpect(jsonPath("$.result.status").value("WAIT"))
                .andExpect(jsonPath("$.result.deliveryTimeLine[0].type").value("SEND_REQUEST"));

        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        DeliveryPoint point = deliveryPointRepository.findByDelivery(delivery).orElseThrow();
        point.setBase_point(1000L);
        point.setDistance_point(2000L);
        point.setWeight_point(500L);
        deliveryPointRepository.saveAndFlush(point);

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
        assertThat(accountRepository.findById(sender.getId()).orElseThrow().getPoint()).isEqualTo(6500L);
        assertThat(pointLogRepository.findAllByAccountOrderByCreatedAtDesc(sender))
                .singleElement()
                .satisfies(log -> {
                    assertThat(log.getIncrementReason()).isEqualTo(PointIncrementReason.DELIVERY_PAYMENT);
                    assertThat(log.getDeltaPoint()).isEqualTo(-3500L);
                    assertThat(log.getBeforePoint()).isEqualTo(10000L);
                    assertThat(log.getAfterPoint()).isEqualTo(6500L);
                });

        mockMvc.perform(get("/account/points")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentPoint").value(6500))
                .andExpect(jsonPath("$.result.pointLogs[0].delivery.id").value(deliveryId))
                .andExpect(jsonPath("$.result.pointLogs[0].delivery.name").value("Laptop"))
                .andExpect(jsonPath("$.result.pointLogs[0].delivery.origin.id").value(sourceId))
                .andExpect(jsonPath("$.result.pointLogs[0].delivery.destination.id").value(destId))
                .andExpect(jsonPath("$.result.pointLogs[0].delivery.status").value("WAIT"))
                .andExpect(jsonPath("$.result.pointLogs[0].incrementReason").value("DELIVERY_PAYMENT"))
                .andExpect(jsonPath("$.result.pointLogs[0].deltaPoint").value(-3500))
                .andExpect(jsonPath("$.result.pointLogs[0].beforePoint").value(10000))
                .andExpect(jsonPath("$.result.pointLogs[0].afterPoint").value(6500));

        mockMvc.perform(patch("/sender/{deliveryId}/cancel", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        entityManager.flush();
        entityManager.clear();
        Delivery canceled = deliveryRepository.findById(deliveryId).orElseThrow();
        assertThat(canceled.getStatus()).isEqualTo(DeliveryState.CANCEL);
        assertThat(accountRepository.findById(sender.getId()).orElseThrow().getPoint()).isEqualTo(10000L);
        assertThat(pointLogRepository.findAllByAccountOrderByCreatedAtDesc(sender))
                .extracting(log -> log.getIncrementReason())
                .containsExactly(PointIncrementReason.DELIVERY_REFUND, PointIncrementReason.DELIVERY_PAYMENT);

        mockMvc.perform(get("/account/points")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.currentPoint").value(10000))
                .andExpect(jsonPath("$.result.pointLogs[0].incrementReason").value("DELIVERY_REFUND"))
                .andExpect(jsonPath("$.result.pointLogs[0].deltaPoint").value(3500))
                .andExpect(jsonPath("$.result.pointLogs[1].incrementReason").value("DELIVERY_PAYMENT"));
        assertThat(deliveryLogRepository.findAllByDeliveryOrderByCreatedAtAsc(canceled))
                .extracting(log -> log.getType())
                .containsExactly(DeliveryLogType.SEND_REQUEST, DeliveryLogType.CANCELED);
    }

    @Test
    void anotherSenderCannotReadOrModifyDelivery() throws Exception {
        Account owner = createAccount("owner");
        Account stranger = createAccount("stranger");
        long deliveryId = createDelivery(accessToken(owner), "Phone", sourceId, destId);
        String strangerToken = accessToken(stranger);

        mockMvc.perform(get("/sender/{deliveryId}", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("DELIVERY403_1"));

        mockMvc.perform(patch("/sender/{deliveryId}/terms", deliveryId)
                        .header("Authorization", bearer(strangerToken)))
                .andExpect(status().isForbidden());
    }

    @Test
    void matchedDeliveryCannotBeCanceled() throws Exception {
        Account sender = createAccount("matched-owner");
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Camera", sourceId, destId);
        Delivery delivery = deliveryRepository.findById(deliveryId).orElseThrow();
        delivery.setStatus(DeliveryState.MATCHED);
        deliveryRepository.saveAndFlush(delivery);

        mockMvc.perform(patch("/sender/{deliveryId}/cancel", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_1"));
    }

    @Test
    void deliveryCannotBeCompletedBeforeConfirmationRequest() throws Exception {
        Account sender = createAccount("early-complete");
        String token = accessToken(sender);
        long deliveryId = createDelivery(token, "Bag", sourceId, destId);

        mockMvc.perform(patch("/sender/{deliveryId}/complete", deliveryId)
                        .header("Authorization", bearer(token)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DELIVERY400_2"));
    }
}
