package com.aicontentstudio.service.impl;

import com.aicontentstudio.dto.response.NotificationResponse;
import com.aicontentstudio.entity.Notification;
import com.aicontentstudio.entity.User;
import com.aicontentstudio.enums.NotificationType;
import com.aicontentstudio.exception.ResourceNotFoundException;
import com.aicontentstudio.exception.UnauthorizedException;
import com.aicontentstudio.repository.NotificationRepository;
import com.aicontentstudio.repository.UserRepository;
import com.aicontentstudio.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void createAndPushNotification(User recipient, User actor, NotificationType type,
                                           String title, String message, String actionUrl,
                                           Long entityId, String entityType) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .actor(actor)
                .type(type)
                .title(title)
                .message(message)
                .actionUrl(actionUrl)
                .entityId(entityId)
                .entityType(entityType)
                .build();

        Notification saved = notificationRepository.save(notification);

        // Push via WebSocket to user-specific queue
        try {
            NotificationResponse payload = NotificationResponse.fromEntity(saved);
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/queue/notifications",
                    payload);
            log.debug("WebSocket notification sent to user: {}", recipient.getEmail());
        } catch (Exception e) {
            log.warn("Failed to push WebSocket notification: {}", e.getMessage());
            // Non-fatal: notification is already persisted in DB
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NotificationResponse> getNotifications(String userEmail, Pageable pageable) {
        User user = getUserByEmail(userEmail);
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(user, pageable)
                .map(NotificationResponse::fromEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(String userEmail) {
        User user = getUserByEmail(userEmail);
        return notificationRepository.countByRecipientAndRead(user, false);
    }

    @Override
    public void markAsRead(Long notificationId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot access this notification");
        }

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String userEmail) {
        User user = getUserByEmail(userEmail);
        notificationRepository.markAllAsReadForUser(user);
    }

    @Override
    public void deleteNotification(Long notificationId, String userEmail) {
        User user = getUserByEmail(userEmail);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId));

        if (!notification.getRecipient().getId().equals(user.getId())) {
            throw new UnauthorizedException("You cannot delete this notification");
        }
        notificationRepository.delete(notification);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }
}
