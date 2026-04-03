package com.spotcamp.module.inventory.entity;

/**
 * Status of a product
 */
public enum ProductStatus {
    /**
     * Product is available for booking/purchase
     */
    ACTIVE,
    
    /**
     * Product is temporarily unavailable
     */
    INACTIVE,
    
    /**
     * Product is no longer available but kept for history
     */
    ARCHIVED
}