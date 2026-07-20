package com.aicontentstudio.service;

import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    void createAndPushNotification(User recipient, User actor, NotificationType type,
                                   String title, String message, String actionUrl,
                                   Long entityId, String entityType);

    Page<NotificationResponse> getNotifications(String userEmail, Pageable pageable);

    long getUnreadCount(String userEmail);

    void markAsRead(Long notificationId, String userEmail);

    void markAllAsRead(String userEmail);

    void deleteNotification(Long notificationId, String userEmail);
}
