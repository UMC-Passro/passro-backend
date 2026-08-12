package com.passro.passrobackend.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.chat.repository.ChatMessageRepository;
import com.passro.passrobackend.chat.repository.ChatRoomRepository;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.support.IntegrationTestSupport;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ChatRoomCreationIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Test
    void firstMessageCreatesChatRoomAndFollowingMessageReusesIt() throws Exception {
        Account sender = createAccount("chat-room-sender");
        Account shipper = createAccount("chat-room-shipper");
        Delivery delivery = deliveryRepository.saveAndFlush(Delivery.builder()
                .sender(sender)
                .shipper(shipper)
                .status(DeliveryState.MATCHED)
                .build());
        String token = accessToken(sender);
        String shipperToken = accessToken(shipper);

        mockMvc.perform(get("/chat/rooms")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());

        String firstResponse = mockMvc.perform(post("/chat/{deliveryId}/messages", delivery.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"첫 메시지\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CHAT201_1"))
                .andExpect(jsonPath("$.result.chatRoom.deliveryId").value(delivery.getId()))
                .andExpect(jsonPath("$.result.content").value("첫 메시지"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        long chatRoomId = objectMapper.readTree(firstResponse).at("/result/chatRoom/id").asLong();

        mockMvc.perform(post("/chat/{deliveryId}/messages", delivery.getId())
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"content\":\"두 번째 메시지\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.chatRoom.id").value(chatRoomId))
                .andExpect(jsonPath("$.result.content").value("두 번째 메시지"));

        mockMvc.perform(get("/chat/rooms")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].chatRoomId").value(chatRoomId))
                .andExpect(jsonPath("$.result[0].deliveryId").value(delivery.getId()))
                .andExpect(jsonPath("$.result[0].lastMessage").value("두 번째 메시지"));

        assertThat(chatRoomRepository.findByDeliveryId(delivery.getId())).isPresent();
        assertThat(chatMessageRepository.findAllByDelivery_IdOrderByCreatedAtAsc(delivery.getId()))
                .hasSize(2);

        mockMvc.perform(delete("/chat/{deliveryId}", delivery.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("CHAT200_2"));

        // 동일 요청은 멱등하게 처리한다.
        mockMvc.perform(delete("/chat/{deliveryId}", delivery.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/chat/rooms")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isEmpty());

        mockMvc.perform(get("/chat/{deliveryId}/messages", delivery.getId())
                        .header("Authorization", bearer(token)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("CHAT403_2"));

        // 상대방의 채팅방과 메시지는 그대로 유지된다.
        mockMvc.perform(get("/chat/rooms")
                        .header("Authorization", bearer(shipperToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.length()").value(1))
                .andExpect(jsonPath("$.result[0].chatRoomId").value(chatRoomId));
    }
}
