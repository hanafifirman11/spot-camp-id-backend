package com.spotcamp.pricing.domain;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entity representing seasonal pricing rules for products
 */
@Entity
@Table(name = "seasonal_pricing_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeasonalPricingRule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 20)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustmentValue;

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Checks if this rule applies to a given date
     */
    public boolean appliesTo(LocalDate date) {
        return active && !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * Checks if this rule applies to a specific product
     */
    public boolean appliesToProduct(Long targetProductId) {
        return productId == null || productId.equals(targetProductId);
    }

    /**
     * Calculates adjusted price
     */
    public BigDecimal calculateAdjustedPrice(BigDecimal basePrice) {
        BigDecimal adjustedPrice;

        if (adjustmentType == AdjustmentType.PERCENTAGE) {
            BigDecimal multiplier = BigDecimal.ONE.add(adjustmentValue.divide(BigDecimal.valueOf(100)));
            adjustedPrice = basePrice.multiply(multiplier);
        } else {
            adjustedPrice = basePrice.add(adjustmentValue);
        }

        // Apply min/max constraints
        if (minPrice != null && adjustedPrice.compareTo(minPrice) < 0) {
            adjustedPrice = minPrice;
        }
        if (maxPrice != null && adjustedPrice.compareTo(maxPrice) > 0) {
            adjustedPrice = maxPrice;
        }

        return adjustedPrice.max(BigDecimal.ZERO);
    }
}
