package com.spotcamp.module.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

/**
 * Entity representing day-of-week based pricing (weekday vs weekend)
 */
@Entity
@Table(name = "day_of_week_pricing")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DayOfWeekPricing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "day_of_week", nullable = false)
    private Integer dayOfWeek;  // 1=Monday, 7=Sunday (ISO)

    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_type", nullable = false, length = 20)
    private AdjustmentType adjustmentType;

    @Column(name = "adjustment_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal adjustmentValue;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if this rule applies to a given day
     */
    public boolean appliesTo(DayOfWeek day) {
        return active && day.getValue() == dayOfWeek;
    }

    /**
     * Checks if this is a weekend day
     */
    public boolean isWeekend() {
        return dayOfWeek == 6 || dayOfWeek == 7;
    }

    /**
     * Calculates adjusted price
     */
    public BigDecimal calculateAdjustedPrice(BigDecimal basePrice) {
        if (adjustmentType == AdjustmentType.PERCENTAGE) {
            BigDecimal multiplier = BigDecimal.ONE.add(adjustmentValue.divide(BigDecimal.valueOf(100)));
            return basePrice.multiply(multiplier).max(BigDecimal.ZERO);
        } else {
            return basePrice.add(adjustmentValue).max(BigDecimal.ZERO);
        }
    }
}
