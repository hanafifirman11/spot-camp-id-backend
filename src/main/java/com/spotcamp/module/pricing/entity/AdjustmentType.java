package com.spotcamp.module.pricing.entity;

/**
 * Type of price adjustment
 */
public enum AdjustmentType {
    /**
     * Percentage adjustment (+20 = 20% increase, -15 = 15% discount)
     */
    PERCENTAGE,

    /**
     * Fixed amount adjustment (+50000 = add 50k, -10000 = subtract 10k)
     */
    FIXED_AMOUNT
}
