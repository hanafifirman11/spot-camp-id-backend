package com.spotcamp.module.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing merchant payouts
 */
@Entity
@Table(name = "merchant_payouts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MerchantPayout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "campsite_id")
    private Long campsiteId;

    @Column(name = "payout_ref", nullable = false, unique = true, length = 100)
    private String payoutRef;

    @Column(name = "external_payout_ref", length = 200)
    private String externalPayoutRef;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "platform_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "payment_gateway_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal paymentGatewayFee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    @Column(name = "booking_count", nullable = false)
    private Integer bookingCount = 0;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "bank_account_number", length = 50)
    private String bankAccountNumber;

    @Column(name = "bank_account_name", length = 200)
    private String bankAccountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private PayoutStatus status = PayoutStatus.PENDING;

    @Column(name = "status_message", length = 500)
    private String statusMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "failed_at")
    private LocalDateTime failedAt;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "payout", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<PayoutItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Calculates net amount from gross minus fees
     */
    public void calculateNetAmount() {
        if (grossAmount != null) {
            this.netAmount = grossAmount
                    .subtract(platformFee != null ? platformFee : BigDecimal.ZERO)
                    .subtract(paymentGatewayFee != null ? paymentGatewayFee : BigDecimal.ZERO);
        }
    }

    /**
     * Approves this payout
     */
    public void approve(Long approvedByUserId) {
        this.status = PayoutStatus.APPROVED;
        this.approvedBy = approvedByUserId;
        this.approvedAt = LocalDateTime.now();
    }

    /**
     * Marks payout as completed
     */
    public void markAsCompleted(String externalRef) {
        this.status = PayoutStatus.COMPLETED;
        this.externalPayoutRef = externalRef;
        this.completedAt = LocalDateTime.now();
    }

    /**
     * Marks payout as failed
     */
    public void markAsFailed(String message) {
        this.status = PayoutStatus.FAILED;
        this.statusMessage = message;
        this.failedAt = LocalDateTime.now();
    }

    /**
     * Payout status
     */
    public enum PayoutStatus {
        PENDING,
        APPROVED,
        PROCESSING,
        COMPLETED,
        FAILED,
        ON_HOLD
    }
}
