package com.spotcamp.notification.api.dto;

import com.spotcamp.notification.domain.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;
    private String title;
    private String message;
    private String priority;
    private boolean read;
    private LocalDateTime createdAt;
    private String referenceType;
    private Long referenceId;
    private Map<String, Object> data;

    public static NotificationResponse from(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .type(notification.getType().name())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority().name())
                .read(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .referenceType(notification.getReferenceType())
                .referenceId(notification.getReferenceId())
                .data(notification.getData())
                .build();
    }
}
