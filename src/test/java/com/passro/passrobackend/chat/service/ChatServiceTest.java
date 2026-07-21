package com.passro.passrobackend.chat.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.chat.dto.ChatMessageRequestDto;
import com.passro.passrobackend.chat.dto.ChatMessageResponseDto;
import com.passro.passrobackend.chat.dto.ChatRoomInfoResponseDto;
import com.passro.passrobackend.chat.entity.ChatMessage;
import com.passro.passrobackend.chat.exception.ChatException;
import com.passro.passrobackend.chat.exception.code.ChatErrorCode;
import com.passro.passrobackend.chat.repository.ChatMessageRepository;
import com.passro.passrobackend.delivery.entity.Delivery;
import com.passro.passrobackend.delivery.entity.DeliveryGoodInfo;
import com.passro.passrobackend.delivery.enums.DeliveryState;
import com.passro.passrobackend.delivery.repository.DeliveryGoodInfoRepository;
import com.passro.passrobackend.delivery.repository.DeliveryRepository;
import com.passro.passrobackend.place.entity.Place;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock ChatMessageRepository chatMessageRepository;
    @Mock DeliveryRepository deliveryRepository;
    @Mock DeliveryGoodInfoRepository deliveryGoodInfoRepository;

    @InjectMocks ChatService chatService;

    Account sender;
    Account shipper;
    Account outsider;
    Delivery delivery;

    @BeforeEach
    void setUp() {
        sender = mock(Account.class);
        given(sender.getId()).willReturn(1L);
        given(sender.getNickname()).willReturn("sender닉네임");
        given(sender.getPicture()).willReturn("sender.jpg");

        shipper = mock(Account.class);
        given(shipper.getId()).willReturn(2L);
        given(shipper.getNickname()).willReturn("shipper닉네임");
        given(shipper.getPicture()).willReturn("shipper.jpg");

        outsider = mock(Account.class);
        given(outsider.getId()).willReturn(99L);

        delivery = mock(Delivery.class);
        given(delivery.getId()).willReturn(1L);
        given(delivery.getSender()).willReturn(sender);
        given(delivery.getShipper()).willReturn(shipper);
        given(delivery.getStatus()).willReturn(DeliveryState.MATCHED);
    }

    // ──────────────────────────────────────────────
    // 접근 권한 검증
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("접근 권한 검증")
    class AccessValidation {

        @Test
        @DisplayName("WAIT 상태 배송건 접근 시 CHAT_NOT_AVAILABLE 예외 발생")
        void waitStatus_throwsChatNotAvailable() {
            given(delivery.getStatus()).willReturn(DeliveryState.WAIT);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));

            assertThatThrownBy(() -> chatService.getMessages(1L, sender))
                    .isInstanceOf(ChatException.class)
                    .hasFieldOrPropertyWithValue("code", ChatErrorCode.CHAT_NOT_AVAILABLE);
        }

        @Test
        @DisplayName("CANCEL 상태 배송건 접근 시 CHAT_NOT_AVAILABLE 예외 발생")
        void cancelStatus_throwsChatNotAvailable() {
            given(delivery.getStatus()).willReturn(DeliveryState.CANCEL);
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));

            assertThatThrownBy(() -> chatService.getMessages(1L, sender))
                    .isInstanceOf(ChatException.class)
                    .hasFieldOrPropertyWithValue("code", ChatErrorCode.CHAT_NOT_AVAILABLE);
        }

        @Test
        @DisplayName("sender도 shipper도 아닌 유저 접근 시 FORBIDDEN_ACCESS 예외 발생")
        void outsider_throwsForbiddenAccess() {
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));

            assertThatThrownBy(() -> chatService.getMessages(1L, outsider))
                    .isInstanceOf(ChatException.class)
                    .hasFieldOrPropertyWithValue("code", ChatErrorCode.FORBIDDEN_ACCESS);
        }

        @Test
        @DisplayName("존재하지 않는 deliveryId 접근 시 DELIVERY_NOT_FOUND 예외 발생")
        void deliveryNotFound_throwsException() {
            given(deliveryRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> chatService.getMessages(999L, sender))
                    .isInstanceOf(ChatException.class)
                    .hasFieldOrPropertyWithValue("code", ChatErrorCode.DELIVERY_NOT_FOUND);
        }
    }

    // ──────────────────────────────────────────────
    // 메시지 조회
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("메시지 조회")
    class GetMessages {

        @Test
        @DisplayName("전체 메시지 조회 시 상대방 메시지 읽음 처리 호출")
        void getMessages_marksAsRead() {
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(chatMessageRepository.findAllByDelivery_IdOrderByCreatedAtAsc(1L)).willReturn(List.of());

            chatService.getMessages(1L, sender);

            then(chatMessageRepository).should().markAllAsRead(1L, sender.getId());
        }

        @Test
        @DisplayName("polling 조회 시 상대방 메시지 읽음 처리 호출")
        void getMessagesAfter_marksAsRead() {
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(chatMessageRepository.findAllByDelivery_IdAndIdGreaterThanOrderByCreatedAtAsc(1L, 3L)).willReturn(List.of());

            chatService.getMessagesAfter(1L, 3L, sender);

            then(chatMessageRepository).should().markAllAsRead(1L, sender.getId());
        }

        @Test
        @DisplayName("전체 메시지 조회 결과 반환")
        void getMessages_returnsMappedDtos() {
            ChatMessage message = mock(ChatMessage.class);
            given(message.getId()).willReturn(1L);
            given(message.getSender()).willReturn(sender);
            given(message.getContent()).willReturn("안녕하세요");

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(chatMessageRepository.findAllByDelivery_IdOrderByCreatedAtAsc(1L)).willReturn(List.of(message));

            List<ChatMessageResponseDto> result = chatService.getMessages(1L, sender);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).content()).isEqualTo("안녕하세요");
        }
    }

    // ──────────────────────────────────────────────
    // 메시지 전송
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("메시지 전송")
    class SendMessage {

        @Test
        @DisplayName("메시지 저장 후 DTO 반환")
        void sendMessage_savesAndReturnsDto() {
            ChatMessage saved = mock(ChatMessage.class);
            given(saved.getId()).willReturn(1L);
            given(saved.getSender()).willReturn(sender);
            given(saved.getContent()).willReturn("테스트 메시지");

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(saved);

            ChatMessageResponseDto result = chatService.sendMessage(1L, new ChatMessageRequestDto("테스트 메시지"), sender);

            assertThat(result.content()).isEqualTo("테스트 메시지");
            then(chatMessageRepository).should().save(any(ChatMessage.class));
        }

        @Test
        @DisplayName("권한 없는 유저는 메시지 전송 불가")
        void sendMessage_outsider_throwsForbiddenAccess() {
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));

            assertThatThrownBy(() -> chatService.sendMessage(1L, new ChatMessageRequestDto("테스트"), outsider))
                    .isInstanceOf(ChatException.class)
                    .hasFieldOrPropertyWithValue("code", ChatErrorCode.FORBIDDEN_ACCESS);

            then(chatMessageRepository).should(never()).save(any());
        }
    }

    // ──────────────────────────────────────────────
    // 채팅방 헤더 정보
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("채팅방 헤더 정보 조회")
    class GetChatRoomInfo {

        @Test
        @DisplayName("sender로 조회 시 partner는 shipper")
        void senderRequest_partnerIsShipper() {
            Place origin = mock(Place.class);
            Place dest = mock(Place.class);
            given(origin.getAddress()).willReturn("서울 강남구");
            given(dest.getAddress()).willReturn("부산 해운대구");
            given(delivery.getOrigin()).willReturn(origin);
            given(delivery.getDest()).willReturn(dest);

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(deliveryGoodInfoRepository.findByDelivery(delivery)).willReturn(Optional.empty());

            ChatRoomInfoResponseDto result = chatService.getChatRoomInfo(1L, sender);

            assertThat(result.partnerNickname()).isEqualTo("shipper닉네임");
            assertThat(result.partnerPicture()).isEqualTo("shipper.jpg");
        }

        @Test
        @DisplayName("shipper로 조회 시 partner는 sender")
        void shipperRequest_partnerIsSender() {
            Place origin = mock(Place.class);
            Place dest = mock(Place.class);
            given(origin.getAddress()).willReturn("서울 강남구");
            given(dest.getAddress()).willReturn("부산 해운대구");
            given(delivery.getOrigin()).willReturn(origin);
            given(delivery.getDest()).willReturn(dest);

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(deliveryGoodInfoRepository.findByDelivery(delivery)).willReturn(Optional.empty());

            ChatRoomInfoResponseDto result = chatService.getChatRoomInfo(1L, shipper);

            assertThat(result.partnerNickname()).isEqualTo("sender닉네임");
            assertThat(result.partnerPicture()).isEqualTo("sender.jpg");
        }

        @Test
        @DisplayName("DeliveryGoodInfo 존재 시 itemName 반환")
        void withGoodInfo_returnsItemName() {
            Place origin = mock(Place.class);
            Place dest = mock(Place.class);
            given(origin.getAddress()).willReturn("서울");
            given(dest.getAddress()).willReturn("부산");
            given(delivery.getOrigin()).willReturn(origin);
            given(delivery.getDest()).willReturn(dest);

            DeliveryGoodInfo goodInfo = mock(DeliveryGoodInfo.class);
            given(goodInfo.getName()).willReturn("노트북");

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(deliveryGoodInfoRepository.findByDelivery(delivery)).willReturn(Optional.of(goodInfo));

            ChatRoomInfoResponseDto result = chatService.getChatRoomInfo(1L, sender);

            assertThat(result.itemName()).isEqualTo("노트북");
        }

        @Test
        @DisplayName("DeliveryGoodInfo 없으면 itemName은 null")
        void withoutGoodInfo_itemNameIsNull() {
            Place origin = mock(Place.class);
            Place dest = mock(Place.class);
            given(origin.getAddress()).willReturn("서울");
            given(dest.getAddress()).willReturn("부산");
            given(delivery.getOrigin()).willReturn(origin);
            given(delivery.getDest()).willReturn(dest);

            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(deliveryGoodInfoRepository.findByDelivery(delivery)).willReturn(Optional.empty());

            ChatRoomInfoResponseDto result = chatService.getChatRoomInfo(1L, sender);

            assertThat(result.itemName()).isNull();
        }
    }

    // ──────────────────────────────────────────────
    // 안읽은 메시지 수
    // ──────────────────────────────────────────────
    @Nested
    @DisplayName("안읽은 메시지 수 조회")
    class GetUnreadCount {

        @Test
        @DisplayName("안읽은 메시지 수 반환")
        void returnsUnreadCount() {
            given(deliveryRepository.findById(1L)).willReturn(Optional.of(delivery));
            given(chatMessageRepository.countByDelivery_IdAndSender_IdNotAndIsReadFalse(1L, sender.getId())).willReturn(3L);

            long count = chatService.getUnreadCount(1L, sender);

            assertThat(count).isEqualTo(3L);
        }
    }
}