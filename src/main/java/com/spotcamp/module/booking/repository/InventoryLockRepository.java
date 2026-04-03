package com.spotcamp.module.booking.repository;

import com.spotcamp.module.booking.entity.InventoryLock;
import com.spotcamp.module.booking.entity.InventoryLock.LockType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for InventoryLock entity operations
 */
@Repository
public interface InventoryLockRepository extends JpaRepository<InventoryLock, Long> {

    /**
     * Find locks for a specific product with pessimistic locking
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT il FROM InventoryLock il WHERE il.productId = :productId")
    List<InventoryLock> findByProductIdWithLock(@Param("productId") Long productId);

    /**
     * Find locks for a specific spot and date range
     */
    @Query("SELECT il FROM InventoryLock il WHERE " +
           "il.spotId = :spotId AND il.lockType = :lockType " +
           "AND NOT (il.endDate <= :checkIn OR il.startDate >= :checkOut)")
    List<InventoryLock> findSpotLocksForDateRange(@Param("spotId") String spotId,
                                                 @Param("lockType") LockType lockType,
                                                 @Param("checkIn") LocalDate checkIn,
                                                 @Param("checkOut") LocalDate checkOut);

    /**
     * Find locks by booking ID
     */
    List<InventoryLock> findByBookingId(Long bookingId);

    /**
     * Find expired locks
     */
    @Query("SELECT il FROM InventoryLock il WHERE il.expiresAt < :now")
    List<InventoryLock> findExpiredLocks(@Param("now") LocalDateTime now);

    /**
     * Find cart locks for a specific product and date range
     */
    @Query("SELECT il FROM InventoryLock il WHERE " +
           "il.productId = :productId AND il.lockType = 'CART' " +
           "AND (:startDate IS NULL OR il.startDate IS NULL OR NOT (il.endDate <= :startDate OR il.startDate >= :endDate))")
    List<InventoryLock> findCartLocksForProduct(@Param("productId") Long productId,
                                               @Param("startDate") LocalDate startDate,
                                               @Param("endDate") LocalDate endDate);

    /**
     * Find confirmed locks for availability checking
     */
    @Query("SELECT il FROM InventoryLock il WHERE " +
           "il.productId = :productId AND il.lockType = 'CONFIRMED' " +
           "AND (:startDate IS NULL OR il.startDate IS NULL OR NOT (il.endDate <= :startDate OR il.startDate >= :endDate))")
    List<InventoryLock> findConfirmedLocksForProduct(@Param("productId") Long productId,
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    /**
     * Count locks for a product in date range
     */
    @Query("SELECT SUM(il.quantity) FROM InventoryLock il WHERE " +
           "il.productId = :productId AND il.lockType = :lockType " +
           "AND (:startDate IS NULL OR :endDate IS NULL OR " +
           "(il.startDate IS NOT NULL AND NOT (il.endDate <= :startDate OR il.startDate >= :endDate)))")
    Optional<Long> countLockedQuantity(@Param("productId") Long productId,
                                     @Param("lockType") LockType lockType,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);

    /**
     * Delete expired cart locks
     */
    @Modifying
    @Query("DELETE FROM InventoryLock il WHERE il.lockType = 'CART' AND il.expiresAt < :cutoffTime")
    int deleteExpiredCartLocks(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Delete locks by booking ID
     */
    @Modifying
    void deleteByBookingId(Long bookingId);

    /**
     * Find locks that will expire soon (for notification)
     */
    @Query("SELECT il FROM InventoryLock il WHERE " +
           "il.lockType = 'CART' AND il.expiresAt BETWEEN :now AND :soonTime")
    List<InventoryLock> findLocksExpiringSoon(@Param("now") LocalDateTime now,
                                            @Param("soonTime") LocalDateTime soonTime);

    /**
     * Check if spot is available for booking (no confirmed locks)
     */
    @Query("SELECT COUNT(il) = 0 FROM InventoryLock il WHERE " +
           "il.spotId = :spotId AND il.lockType = 'CONFIRMED' " +
           "AND NOT (il.endDate <= :checkIn OR il.startDate >= :checkOut)")
    boolean isSpotAvailableForDateRange(@Param("spotId") String spotId,
                                      @Param("checkIn") LocalDate checkIn,
                                      @Param("checkOut") LocalDate checkOut);

    /**
     * Get total locked quantity for a product
     */
    @Query("SELECT COALESCE(SUM(il.quantity), 0) FROM InventoryLock il WHERE " +
           "il.productId = :productId AND il.lockType = 'CONFIRMED'")
    long getTotalLockedQuantity(@Param("productId") Long productId);

    /**
     * Convert cart locks to confirmed locks for a booking
     */
    @Modifying
    @Query("UPDATE InventoryLock il SET il.lockType = 'CONFIRMED', il.expiresAt = :newExpiry " +
           "WHERE il.bookingId = :bookingId AND il.lockType = 'CART'")
    int confirmLocksForBooking(@Param("bookingId") Long bookingId, @Param("newExpiry") LocalDateTime newExpiry);

    /**
     * Convert cart locks for spots (rental items) to confirmed locks.
     */
    @Modifying
    @Query("UPDATE InventoryLock il SET il.lockType = 'CONFIRMED', il.expiresAt = :newExpiry " +
           "WHERE il.bookingId = :bookingId AND il.lockType = 'CART' " +
           "AND (il.spotId IS NOT NULL OR il.startDate IS NOT NULL)")
    int confirmRentalLocksForBooking(@Param("bookingId") Long bookingId, @Param("newExpiry") LocalDateTime newExpiry);

    /**
     * Remove sale product locks after stock is deducted.
     */
    @Modifying
    @Query("DELETE FROM InventoryLock il WHERE il.bookingId = :bookingId " +
           "AND il.spotId IS NULL AND il.startDate IS NULL")
    int deleteSaleLocksForBooking(@Param("bookingId") Long bookingId);

    /**
     * Extend cart lock expiry time
     */
    @Modifying
    @Query("UPDATE InventoryLock il SET il.expiresAt = :newExpiry " +
           "WHERE il.bookingId = :bookingId AND il.lockType = 'CART'")
    int extendCartLocks(@Param("bookingId") Long bookingId, @Param("newExpiry") LocalDateTime newExpiry);
}
