package com.spotcamp.payment.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Entity representing individual bookings included in a merchant payout
 */
@Entity
@Table(name = "payout_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayoutItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payout_id", nullable = false)
    private MerchantPayout payout;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "payment_transaction_id", nullable = false)
    private Long paymentTransactionId;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "platform_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal platformFee = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal netAmount;

    /**
     * Calculates net amount
     */
    public void calculateNetAmount() {
        if (amount != null) {
            this.netAmount = amount.subtract(platformFee != null ? platformFee : BigDecimal.ZERO);
        }
    }
}
