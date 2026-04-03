package com.spotcamp.inventory.domain;

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