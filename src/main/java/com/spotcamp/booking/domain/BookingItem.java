package com.spotcamp.booking.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Individual item within a booking (spot rental or product purchase)
 */
@Entity
@Table(name = "booking_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Column(name = "product_type", nullable = false, length = 20)
    private String productType;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    /**
     * Validates item data
     */
    public boolean isValid() {
        return productId != null &&
               productName != null && !productName.trim().isEmpty() &&
               quantity != null && quantity > 0 &&
               unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) >= 0 &&
               subtotal != null && subtotal.compareTo(BigDecimal.ZERO) >= 0;
    }

    /**
     * Recalculates subtotal based on quantity and unit price
     */
    public void recalculateSubtotal() {
        this.subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}
