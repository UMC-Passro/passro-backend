package com.passro.passrobackend.notification.service;

import com.passro.passrobackend.account.entity.Account;
import com.passro.passrobackend.notification.code.NotificationErrorCode;
import com.passro.passrobackend.notification.dto.NotificationResponseDto;
import com.passro.passrobackend.notification.dto.UnreadCountResponseDto;
import com.passro.passrobackend.notification.entity.Notification;
import com.passro.passrobackend.notification.enums.NotificationType;
import com.passro.passrobackend.notification.enums.ResourceType;
import com.passro.passrobackend.notification.exception.NotificationException;
import com.passro.passrobackend.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 발행 (다른 도메인 서비스가 호출)
     * @param recipient 알림 수신자
     * @param type 알림 종류
     * @param title 알림 제목
     * @param content 알림 내용 (nullable)
     * @param resourceType 클릭 시 이동할 자원 종류 (NONE 이면 이동 없음)
     * @param resourceId 자원 ID (resourceType 이 NONE 이면 null)
     */
    @Transactional
    public Notification publish(Account recipient,
                                NotificationType type,
                                String title,
                                String content,
                                ResourceType resourceType,
                                Long resourceId) {
        Notification notification = Notification.builder()
                .account(recipient)
                .type(type)
                .title(title)
                .content(content)
                .resourceType(resourceType != null ? resourceType : ResourceType.NONE)
                .resourceId(resourceId)
                .isRead(false)
                .build();
        return notificationRepository.save(notification);
    }

    /**
     * 내 알림 목록 조회 (최신순, 페이지네이션)
     */
    @Transactional(readOnly = true)
    public Page<NotificationResponseDto> getMyNotifications(Account account, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return notificationRepository.findAllByAccountOrderByCreatedAtDesc(account, pageable)
                .map(NotificationResponseDto::fromNotification);
    }

    /**
     * 미확인 알림 수
     */
    @Transactional(readOnly = true)
    public UnreadCountResponseDto getUnreadCount(Account account) {
        long count = notificationRepository.countByAccountAndIsReadFalse(account);
        return UnreadCountResponseDto.builder().unreadCount(count).build();
    }

    /**
     * 개별 알림 확인 처리 (본인 알림만)
     */
    @Transactional
    public NotificationResponseDto markAsRead(Account account, Long notificationId) {
        Notification notification = findOwnedNotification(account, notificationId);
        notification.markAsRead();
        return NotificationResponseDto.fromNotification(notification);
    }

    /**
     * 개별 알림 삭제 (본인 알림만)
     */
    @Transactional
    public void deleteNotification(Account account, Long notificationId) {
        Notification notification = findOwnedNotification(account, notificationId);
        notificationRepository.delete(notification);
    }

    // 본인 소유 확인 헬퍼
    private Notification findOwnedNotification(Account account, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOT_FOUND));
        if (!notification.getAccount().getId().equals(account.getId())) {
            throw new NotificationException(NotificationErrorCode.FORBIDDEN);
        }
        return notification;
    }
}
