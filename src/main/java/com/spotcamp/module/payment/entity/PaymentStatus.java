package com.spotcamp.module.payment.entity;

/**
 * Status enum for payment transactions
 */
public enum PaymentStatus {
    /**
     * Payment initiated, waiting for processing
     */
    PENDING,

    /**
     * Payment instruction sent, waiting for user to pay
     */
    AWAITING_PAYMENT,

    /**
     * Payment is being processed
     */
    PROCESSING,

    /**
     * Payment completed successfully
     */
    SUCCESS,

    /**
     * Payment failed
     */
    FAILED,

    /**
     * Payment expired (user didn't pay in time)
     */
    EXPIRED,

    /**
     * Payment cancelled by user or system
     */
    CANCELLED,

    /**
     * Payment has been fully refunded
     */
    REFUNDED,

    /**
     * Payment has been partially refunded
     */
    PARTIAL_REFUND
}
