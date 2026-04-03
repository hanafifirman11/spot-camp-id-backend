package com.spotcamp.payment.domain;

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
 * Entity representing payment transactions (DOKU integration)
 */
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 100)
    private String invoiceNumber;

    @Column(name = "external_ref", length = 200)
    private String externalRef;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "IDR";

    @Column(name = "fee_amount", precision = 12, scale = 2)
    private BigDecimal feeAmount = BigDecimal.ZERO;

    @Column(name = "net_amount", precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "payment_method", nullable = false, length = 50)
    private String paymentMethod;

    @Column(name = "payment_channel", length = 50)
    private String paymentChannel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "doku_request_id", length = 200)
    private String dokuRequestId;

    @Column(name = "doku_response_code", length = 50)
    private String dokuResponseCode;

    @Column(name = "doku_va_number", length = 50)
    private String dokuVaNumber;

    @Column(name = "doku_qris_string", columnDefinition = "TEXT")
    private String dokuQrisString;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "request_payload")
    private Map<String, Object> requestPayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "response_payload")
    private Map<String, Object> responsePayload;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "webhook_payload")
    private Map<String, Object> webhookPayload;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if payment is successful
     */
    public boolean isSuccess() {
        return status == PaymentStatus.SUCCESS;
    }

    /**
     * Checks if payment is pending
     */
    public boolean isPending() {
        return status == PaymentStatus.PENDING || status == PaymentStatus.AWAITING_PAYMENT;
    }

    /**
     * Checks if payment has expired
     */
    public boolean isExpired() {
        return status == PaymentStatus.EXPIRED ||
               (expiresAt != null && LocalDateTime.now().isAfter(expiresAt) && isPending());
    }

    /**
     * Marks payment as successful
     */
    public void markAsSuccess(String externalRef) {
        this.status = PaymentStatus.SUCCESS;
        this.externalRef = externalRef;
        this.paidAt = LocalDateTime.now();
        calculateNetAmount();
    }

    /**
     * Marks payment as failed
     */
    public void markAsFailed(String message) {
        this.status = PaymentStatus.FAILED;
        this.statusMessage = message;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Marks payment as expired
     */
    public void markAsExpired() {
        this.status = PaymentStatus.EXPIRED;
        this.statusMessage = "Payment expired";
    }

    /**
     * Calculates net amount after fees
     */
    public void calculateNetAmount() {
        if (amount != null && feeAmount != null) {
            this.netAmount = amount.subtract(feeAmount);
        }
    }
}
