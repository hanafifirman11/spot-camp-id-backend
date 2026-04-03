package com.spotcamp.module.booking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inventory lock entity for pessimistic locking of spots and products
 * Prevents race conditions during booking process
 */
@Entity
@Table(name = "inventory_locks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "spot_id", length = 100)
    private String spotId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "lock_type", nullable = false)
    private LockType lockType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    /**
     * Checks if this lock has expired
     */
    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Checks if this is a cart lock
     */
    public boolean isCartLock() {
        return lockType == LockType.CART;
    }

    /**
     * Checks if this is a confirmed lock
     */
    public boolean isConfirmedLock() {
        return lockType == LockType.CONFIRMED;
    }

    /**
     * Extends the expiry time (for cart extensions)
     */
    public void extendExpiry(int minutes) {
        if (isCartLock()) {
            this.expiresAt = this.expiresAt.plusMinutes(minutes);
        }
    }

    /**
     * Converts cart lock to confirmed lock
     */
    public void confirmLock() {
        this.lockType = LockType.CONFIRMED;
        // Confirmed locks have much longer expiry (until checkout date + buffer)
        if (endDate != null) {
            this.expiresAt = endDate.plusDays(1).atStartOfDay();
        } else {
            this.expiresAt = LocalDateTime.now().plusDays(30); // Default 30 days for sale items
        }
    }

    /**
     * Checks if this lock conflicts with a date range
     */
    public boolean conflictsWith(LocalDate checkStart, LocalDate checkEnd) {
        if (startDate == null || endDate == null) {
            return false; // Sale items don't have date conflicts
        }
        
        // Check for date overlap
        return !(endDate.isBefore(checkStart) || startDate.isAfter(checkEnd));
    }

    /**
     * Type of inventory lock
     */
    public enum LockType {
        /**
         * Temporary lock while item is in cart (15 minutes)
         */
        CART,
        
        /**
         * Permanent lock for confirmed bookings
         */
        CONFIRMED
    }
}
