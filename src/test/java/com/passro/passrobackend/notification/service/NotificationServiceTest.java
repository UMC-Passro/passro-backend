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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    private Account account(Long id) {
        return Account.builder().id(id).nickname("tester").build();
    }

    private Notification notification(Long id, Account owner, boolean isRead) {
        return Notification.builder()
                .id(id)
                .account(owner)
                .type(NotificationType.DELIVERY)
                .title("배송 상태 업데이트")
                .content("매칭이 완료되었습니다.")
                .resourceType(ResourceType.DELIVERY)
                .resourceId(100L)
                .isRead(isRead)
                .build();
    }

    @Test
    @DisplayName("알림 발행 성공")
    void publish_success() {
        // given
        Account recipient = account(10L);
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Notification result = notificationService.publish(
                recipient,
                NotificationType.DELIVERY,
                "매칭 완료",
                "발송 요청이 매칭되었습니다.",
                ResourceType.DELIVERY,
                123L);

        // then
        assertThat(result.getAccount()).isEqualTo(recipient);
        assertThat(result.getType()).isEqualTo(NotificationType.DELIVERY);
        assertThat(result.getTitle()).isEqualTo("매칭 완료");
        assertThat(result.getResourceType()).isEqualTo(ResourceType.DELIVERY);
        assertThat(result.getResourceId()).isEqualTo(123L);
        assertThat(result.isRead()).isFalse();
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 발행 - resourceType 이 null 이면 NONE 으로 저장하고 resourceId 도 null 로 정리")
    void publish_nullResourceTypeClearsResourceId() {
        // given
        Account recipient = account(10L);
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when — resourceType 은 null 인데 resourceId 는 넘어옴
        Notification result = notificationService.publish(
                recipient, NotificationType.GENERAL, "공지", null, null, 999L);

        // then — resourceType 은 NONE 으로 저장, resourceId 는 null 로 정리됨
        assertThat(result.getResourceType()).isEqualTo(ResourceType.NONE);
        assertThat(result.getResourceId()).isNull();
    }

    @Test
    @DisplayName("알림 발행 - resourceType 이 명시적으로 NONE 이면 resourceId 도 null 로 정리")
    void publish_explicitNoneClearsResourceId() {
        // given
        Account recipient = account(10L);
        given(notificationRepository.save(any(Notification.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        Notification result = notificationService.publish(
                recipient, NotificationType.GENERAL, "공지", null, ResourceType.NONE, 555L);

        // then
        assertThat(result.getResourceType()).isEqualTo(ResourceType.NONE);
        assertThat(result.getResourceId()).isNull();
    }

    @Test
    @DisplayName("내 알림 목록 조회 성공")
    void getMyNotifications_success() {
        // given
        Account me = account(10L);
        Notification n1 = notification(101L, me, false);
        Notification n2 = notification(102L, me, true);
        Page<Notification> page = new PageImpl<>(List.of(n1, n2));
        given(notificationRepository.findAllByAccountOrderByCreatedAtDesc(any(Account.class), any(Pageable.class)))
                .willReturn(page);

        // when
        Page<NotificationResponseDto> result = notificationService.getMyNotifications(me, 0, 20);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).getNotificationId()).isEqualTo(101L);
    }

    @Test
    @DisplayName("미확인 알림 수 조회 성공")
    void getUnreadCount_success() {
        // given
        Account me = account(10L);
        given(notificationRepository.countByAccountAndIsReadFalse(me)).willReturn(3L);

        // when
        UnreadCountResponseDto result = notificationService.getUnreadCount(me);

        // then
        assertThat(result.getUnreadCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("알림 확인 처리 성공")
    void markAsRead_success() {
        // given
        Account me = account(10L);
        Notification target = notification(1L, me, false);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(target));

        // when
        NotificationResponseDto result = notificationService.markAsRead(me, 1L);

        // then
        assertThat(result.isRead()).isTrue();
        assertThat(target.isRead()).isTrue();
        assertThat(target.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("알림 확인 실패 - 존재하지 않는 알림")
    void markAsRead_notFound() {
        // given
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(account(10L), 999L))
                .isInstanceOf(NotificationException.class)
                .extracting(e -> ((NotificationException) e).getCode())
                .isEqualTo(NotificationErrorCode.NOT_FOUND);
    }

    @Test
    @DisplayName("알림 확인 실패 - 남의 알림 조작 시도")
    void markAsRead_forbidden() {
        // given
        Account other = account(999L);
        Notification target = notification(1L, other, false);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(target));

        // when & then
        assertThatThrownBy(() -> notificationService.markAsRead(account(10L), 1L))
                .isInstanceOf(NotificationException.class)
                .extracting(e -> ((NotificationException) e).getCode())
                .isEqualTo(NotificationErrorCode.FORBIDDEN);
    }

    @Test
    @DisplayName("알림 삭제 성공")
    void deleteNotification_success() {
        // given
        Account me = account(10L);
        Notification target = notification(1L, me, true);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(target));

        // when
        notificationService.deleteNotification(me, 1L);

        // then
        verify(notificationRepository).delete(target);
    }

    @Test
    @DisplayName("알림 삭제 실패 - 남의 알림 삭제 시도")
    void deleteNotification_forbidden() {
        // given
        Account other = account(999L);
        Notification target = notification(1L, other, false);
        given(notificationRepository.findById(1L)).willReturn(Optional.of(target));

        // when & then
        assertThatThrownBy(() -> notificationService.deleteNotification(account(10L), 1L))
                .isInstanceOf(NotificationException.class);
        verify(notificationRepository, never()).delete(any());
    }
}
