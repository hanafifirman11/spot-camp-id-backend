package com.spotcamp.module.booking.entity;

/**
 * Status of a booking throughout its lifecycle
 */
public enum BookingStatus {
    /**
     * Items are in user's cart but not yet checked out
     */
    IN_CART,
    
    /**
     * Checkout initiated, waiting for payment completion
     */
    PAYMENT_PENDING,
    
    /**
     * Payment successful, booking is confirmed
     */
    CONFIRMED,
    
    /**
     * Booking was cancelled by user or system
     */
    CANCELLED,
    
    /**
     * Booking period has ended, all services completed
     */
    COMPLETED
}