package com.spotcamp.pricing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entity representing special date pricing (holidays, events)
 */
@Entity
@Table(name = "special_date_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialDatePricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "special_date", nullable = false)
    private LocalDate specialDate;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 20)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustmentValue;

    @Column(name = "override_price", precision = 12, scale = 2)
    private BigDecimal overridePrice;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this rule applies to a given date
     */
    public boolean appliesTo(LocalDate date) {
        return active && specialDate.equals(date);
    }

    /**
     * Checks if there's a price override
     */
    public boolean hasOverridePrice() {
        return overridePrice != null;
    }

    /**
     * Gets the final price for this special date
     */
    public BigDecimal getFinalPrice(BigDecimal basePrice) {
        if (hasOverridePrice()) {
            return overridePrice;
        }

        if (adjustmentType == AdjustmentType.PERCENTAGE) {
            BigDecimal multiplier = BigDecimal.ONE.add(adjustmentValue.divide(BigDecimal.valueOf(100)));
            return basePrice.multiply(multiplier).max(BigDecimal.ZERO);
        } else {
            return basePrice.add(adjustmentValue).max(BigDecimal.ZERO);
        }
    }
}
