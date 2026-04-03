package com.spotcamp.notification.domain;

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
 * Entity representing email delivery logs
 */
@Entity
@Table(name = "email_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "recipient_email", nullable = false, length = 255)
    private String recipientEmail;

    @Column(name = "template_id", nullable = false, length = 100)
    private String templateId;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EmailStatus status = EmailStatus.PENDING;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "template_data")
    private Map<String, Object> templateData;

    @Column(name = "html_content", columnDefinition = "TEXT")
    private String htmlContent;

    @Column(name = "provider", length = 50)
    private String provider = "SENDGRID";

    @Column(name = "provider_message_id", length = 200)
    private String providerMessageId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "opened_at")
    private LocalDateTime openedAt;

    @Column(name = "clicked_at")
    private LocalDateTime clickedAt;

    @Column(name = "bounced_at")
    private LocalDateTime bouncedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "retry_count", nullable = false)
    private Integer retryCount = 0;

    @Column(name = "last_retry_at")
    private LocalDateTime lastRetryAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Marks email as sent
     */
    public void markAsSent(String messageId) {
        this.status = EmailStatus.SENT;
        this.providerMessageId = messageId;
        this.sentAt = LocalDateTime.now();
    }

    /**
     * Marks email as delivered
     */
    public void markAsDelivered() {
        this.status = EmailStatus.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Marks email as failed
     */
    public void markAsFailed(String message) {
        this.status = EmailStatus.FAILED;
        this.statusMessage = message;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Increments retry count
     */
    public void incrementRetry() {
        this.retryCount++;
        this.lastRetryAt = LocalDateTime.now();
    }

    /**
     * Email status enum
     */
    public enum EmailStatus {
        PENDING,
        QUEUED,
        SENT,
        DELIVERED,
        OPENED,
        CLICKED,
        BOUNCED,
        FAILED,
        SPAM
    }
}
