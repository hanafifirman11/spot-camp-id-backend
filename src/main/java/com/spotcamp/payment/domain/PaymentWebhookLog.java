package com.spotcamp.payment.domain;

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
 * Entity representing payment webhook logs for audit
 */
@Entity
@Table(name = "payment_webhook_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "webhook_id", length = 200)
    private String webhookId;

    @Column(name = "provider", nullable = false, length = 50)
    private String provider = "DOKU";

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "transaction_ref", length = 200)
    private String transactionRef;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false)
    private Map<String, Object> payload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "headers")
    private Map<String, Object> headers;

    @Column(name = "signature", length = 500)
    private String signature;

    @Column(name = "signature_valid")
    private Boolean signatureValid;

    @Column(name = "processed", nullable = false)
    private boolean processed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processing_result", length = 500)
    private String processingResult;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "received_at", nullable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }

    /**
     * Marks webhook as processed
     */
    public void markAsProcessed(String result) {
        this.processed = true;
        this.processedAt = LocalDateTime.now();
        this.processingResult = result;
    }

    /**
     * Checks if signature is valid
     */
    public boolean hasValidSignature() {
        return Boolean.TRUE.equals(signatureValid);
    }
}
