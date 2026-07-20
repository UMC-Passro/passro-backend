package com.passro.passrobackend.chat.repository;

import com.passro.passrobackend.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findAllByDelivery_IdOrderByCreatedAtAsc(Long deliveryId);

    List<ChatMessage> findAllByDelivery_IdAndIdGreaterThanOrderByCreatedAtAsc(Long deliveryId, Long afterId);

    // 상대방이 보낸 안읽은 메시지를 일괄 읽음 처리
    @Modifying
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.delivery.id = :deliveryId AND m.sender.id != :readerId AND m.isRead = false")
    void markAllAsRead(@Param("deliveryId") Long deliveryId, @Param("readerId") Long readerId);

    // 안읽은 메시지 수 (상대방이 보낸 것 중 안읽은 것)
    long countByDelivery_IdAndSender_IdNotAndIsReadFalse(Long deliveryId, Long readerId);
}