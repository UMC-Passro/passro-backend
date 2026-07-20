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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final DeliveryRepository deliveryRepository;
    private final DeliveryGoodInfoRepository deliveryGoodInfoRepository;

    // 채팅 참여자 여부 검증 (sender 또는 shipper만 접근 가능)
    private Delivery getDeliveryAndValidateAccess(Long deliveryId, Account account) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.DELIVERY_NOT_FOUND));

        if (delivery.getStatus() == DeliveryState.WAIT || delivery.getStatus() == DeliveryState.CANCEL) {
            throw new ChatException(ChatErrorCode.CHAT_NOT_AVAILABLE);
        }

        boolean isSender = delivery.getSender() != null && delivery.getSender().getId().equals(account.getId());
        boolean isShipper = delivery.getShipper() != null && delivery.getShipper().getId().equals(account.getId());

        if (!isSender && !isShipper) {
            throw new ChatException(ChatErrorCode.FORBIDDEN_ACCESS);
        }

        return delivery;
    }

    // 메시지 전체 조회 (최초 진입) — 조회 시 상대방 메시지 읽음 처리
    @Transactional
    public List<ChatMessageResponseDto> getMessages(Long deliveryId, Account account) {
        getDeliveryAndValidateAccess(deliveryId, account);
        chatMessageRepository.markAllAsRead(deliveryId, account.getId());
        return chatMessageRepository.findAllByDelivery_IdOrderByCreatedAtAsc(deliveryId)
                .stream()
                .map(ChatMessageResponseDto::from)
                .toList();
    }

    // polling: lastMessageId 이후 새 메시지만 조회 — 조회 시 상대방 메시지 읽음 처리
    @Transactional
    public List<ChatMessageResponseDto> getMessagesAfter(Long deliveryId, Long afterId, Account account) {
        getDeliveryAndValidateAccess(deliveryId, account);
        chatMessageRepository.markAllAsRead(deliveryId, account.getId());
        return chatMessageRepository.findAllByDelivery_IdAndIdGreaterThanOrderByCreatedAtAsc(deliveryId, afterId)
                .stream()
                .map(ChatMessageResponseDto::from)
                .toList();
    }

    // 안읽은 메시지 수 조회
    @Transactional(readOnly = true)
    public long getUnreadCount(Long deliveryId, Account account) {
        getDeliveryAndValidateAccess(deliveryId, account);
        return chatMessageRepository.countByDelivery_IdAndSender_IdNotAndIsReadFalse(deliveryId, account.getId());
    }

    // 채팅방 헤더 정보 조회
    @Transactional(readOnly = true)
    public ChatRoomInfoResponseDto getChatRoomInfo(Long deliveryId, Account account) {
        Delivery delivery = getDeliveryAndValidateAccess(deliveryId, account);

        boolean isSender = delivery.getSender().getId().equals(account.getId());
        Account partner = isSender ? delivery.getShipper() : delivery.getSender();

        DeliveryGoodInfo goodInfo = deliveryGoodInfoRepository.findByDelivery(delivery).orElse(null);

        return new ChatRoomInfoResponseDto(
                partner.getNickname(),
                partner.getPicture(),
                goodInfo != null ? goodInfo.getName() : null,
                delivery.getOrigin() != null ? delivery.getOrigin().getAddress() : null,
                delivery.getDest() != null ? delivery.getDest().getAddress() : null,
                delivery.getStatus()
        );
    }

    // 메시지 전송
    @Transactional
    public ChatMessageResponseDto sendMessage(Long deliveryId, ChatMessageRequestDto request, Account account) {
        Delivery delivery = getDeliveryAndValidateAccess(deliveryId, account);

        ChatMessage message = ChatMessage.builder()
                .delivery(delivery)
                .sender(account)
                .content(request.content())
                .build();

        return ChatMessageResponseDto.from(chatMessageRepository.save(message));
    }
}