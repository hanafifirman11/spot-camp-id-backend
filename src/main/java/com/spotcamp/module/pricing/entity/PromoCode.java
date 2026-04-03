package com.spotcamp.module.pricing.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity representing promotional discount codes
 */
@Entity
@Table(name = "promo_codes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromoCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "campsite_id")
    private Long campsiteId;

    @Column(name = "product_id")
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private AdjustmentType discountType;

    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "min_order_amount", precision = 12, scale = 2)
    private BigDecimal minOrderAmount;

    @Column(name = "total_usage_limit")
    private Integer totalUsageLimit;

    @Column(name = "per_user_limit")
    private Integer perUserLimit = 1;

    @Column(name = "current_usage", nullable = false)
    private Integer currentUsage = 0;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "first_booking_only", nullable = false)
    private boolean firstBookingOnly = false;

    @Column(name = "applicable_days")
    private Integer[] applicableDays;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "promoCode", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PromoCodeUsage> usages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
        if (code != null) {
            code = code.toUpperCase();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Checks if promo code is currently valid
     */
    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startDate) && now.isBefore(endDate);
    }

    /**
     * Checks if usage limit is reached
     */
    public boolean isUsageLimitReached() {
        return totalUsageLimit != null && currentUsage >= totalUsageLimit;
    }

    /**
     * Checks if promo can be applied to order amount
     */
    public boolean canApplyToAmount(BigDecimal orderAmount) {
        return minOrderAmount == null || orderAmount.compareTo(minOrderAmount) >= 0;
    }

    /**
     * Calculates discount amount for given order total
     */
    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal discount;

        if (discountType == AdjustmentType.PERCENTAGE) {
            discount = orderAmount.multiply(discountValue.divide(BigDecimal.valueOf(100)));
            if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
                discount = maxDiscountAmount;
            }
        } else {
            discount = discountValue.min(orderAmount);
        }

        return discount;
    }

    /**
     * Increments usage count
     */
    public void incrementUsage() {
        this.currentUsage++;
    }
}
