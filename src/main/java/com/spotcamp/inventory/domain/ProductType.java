package com.spotcamp.inventory.domain;

/**
 * Type of product in the hybrid inventory system
 */
public enum ProductType {
    /**
     * Rental map spots (visual map reservations)
     * Managed by time-based availability per spot
     */
    RENTAL_SPOT,

    /**
     * Rental items (equipment rentals with returning stock)
     * Managed by time-based availability with stock totals
     */
    RENTAL_ITEM,
    
    /**
     * Sale items (consumables, merchandise)
     * Managed by quantity-based inventory
     */
    SALE
}
