package com.passro.passrobackend.notification.dto;

import com.passro.passrobackend.notification.entity.Notification;
import com.passro.passrobackend.notification.enums.NotificationType;
import com.passro.passrobackend.notification.enums.ResourceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDto {
    private Long notificationId;
    private NotificationType type;
    private String title;
    private String content;
    private ResourceType resourceType;
    private Long resourceId;
    private boolean isRead;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;

    public static NotificationResponseDto fromNotification(Notification n) {
        return NotificationResponseDto.builder()
                .notificationId(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .content(n.getContent())
                .resourceType(n.getResourceType())
                .resourceId(n.getResourceId())
                .isRead(n.isRead())
                .readAt(n.getReadAt())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
