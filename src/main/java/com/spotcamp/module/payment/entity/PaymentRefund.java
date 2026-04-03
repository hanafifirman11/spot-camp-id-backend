package com.spotcamp.module.payment.entity;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing payment refunds
 */
@Entity
@Table(name = "payment_refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_transaction_id", nullable = false)
    private Long paymentTransactionId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "refund_ref", nullable = false, unique = true, length = 100)
    private String refundRef;

    @Column(name = "external_refund_ref", length = 200)
    private String externalRefundRef;

    @Column(name = "refund_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal refundAmount;

    @Column(name = "original_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 20)
    private RefundType refundType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private RefundStatus status = RefundStatus.PENDING;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason", nullable = false, length = 50)
    private RefundReason reason;

    @Column(name = "reason_detail", columnDefinition = "TEXT")
    private String reasonDetail;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload")
    private Map<String, Object> responsePayload;

    @Column(name = "initiated_by")
    private Long initiatedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if refund is complete
     */
    public boolean isCompleted() {
        return status == RefundStatus.COMPLETED;
    }

    /**
     * Checks if this is a full refund
     */
    public boolean isFullRefund() {
        return refundType == RefundType.FULL;
    }

    /**
     * Marks refund as completed
     */
    public void markAsCompleted(String externalRef) {
        this.status = RefundStatus.COMPLETED;
        this.externalRefundRef = externalRef;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marks refund as failed
     */
    public void markAsFailed(String message) {
        this.status = RefundStatus.FAILED;
        this.statusMessage = message;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Refund types
     */
    public enum RefundType {
        FULL,
        PARTIAL
    }

    /**
     * Refund status
     */
    public enum RefundStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        REJECTED
    }

    /**
     * Refund reasons
     */
    public enum RefundReason {
        CUSTOMER_REQUEST,
        BOOKING_CANCELLED,
        DUPLICATE_PAYMENT,
        SERVICE_ISSUE,
        ADMIN_INITIATED,
        OTHER
    }
}
