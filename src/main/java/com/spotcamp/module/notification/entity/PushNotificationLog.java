package com.spotcamp.module.notification.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing push notification delivery logs
 */
@Entity
@Table(name = "push_notification_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "notification_id")
    private Long notificationId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "data")
    private Map<String, Object> data;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PushStatus status = PushStatus.PENDING;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @Column(name = "provider", length = 50)
    private String provider = "FCM";

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Marks as sent
     */
    public void markAsSent(String messageId) {
        this.status = PushStatus.SENT;
        this.providerMessageId = messageId;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marks as failed
     */
    public void markAsFailed(String message) {
        this.status = PushStatus.FAILED;
        this.statusMessage = message;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Push notification status
     */
    public enum PushStatus {
        PENDING,
        SENT,
        DELIVERED,
        FAILED,
        INVALID_TOKEN
    }
}
