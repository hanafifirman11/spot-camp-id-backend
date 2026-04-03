package com.spotcamp.module.notification.service;

import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.module.notification.entity.Notification;
import com.spotcamp.module.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<Notification> listByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Notification markAsRead(Long userId, Long notificationId) {
        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new BusinessException("Notification not found"));
        notification.markAsRead();
        return notificationRepository.save(notification);
    }

    public Notification createNotification(Long userId,
                                           Notification.NotificationType type,
                                           String title,
                                           String message,
                                           Notification.NotificationPriority priority,
                                           Map<String, Object> data,
                                           String referenceType,
                                           Long referenceId) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .message(message)
                .priority(priority)
                .data(data)
                .referenceType(referenceType)
                .referenceId(referenceId)
                .build();
        return notificationRepository.save(notification);
    }
}
