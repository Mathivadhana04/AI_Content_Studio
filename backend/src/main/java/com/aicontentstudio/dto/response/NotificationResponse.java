package com.aicontentstudio.dto.response;

import com.aicontentstudio.entity.Notification;
import com.aicontentstudio.enums.NotificationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private String actionUrl;
    private boolean read;
    private String actorName;
    private String actorAvatar;
    private Long entityId;
    private String entityType;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .title(n.getTitle())
                .message(n.getMessage())
                .actionUrl(n.getActionUrl())
                .read(n.isRead())
                .actorName(n.getActor() != null ? n.getActor().getFullName() : "System")
                .actorAvatar(n.getActor() != null ? n.getActor().getAvatarUrl() : null)
                .entityId(n.getEntityId())
                .entityType(n.getEntityType())
                .createdAt(n.getCreatedAt())
                .build();
    }
}
