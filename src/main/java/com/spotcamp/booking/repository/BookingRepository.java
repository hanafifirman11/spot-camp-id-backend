package com.spotcamp.booking.repository;

import com.spotcamp.booking.domain.Booking;
import com.spotcamp.booking.domain.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Booking entity operations with pessimistic locking support
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * Find user's current cart (IN_CART status)
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId AND b.status = 'IN_CART'")
    @EntityGraph(attributePaths = "items")
    Optional<Booking> findUserCartWithLock(@Param("userId") Long userId);

    /**
     * Find user's current cart (without lock for reading)
     */
    @EntityGraph(attributePaths = "items")
    Optional<Booking> findByUserIdAndStatus(Long userId, BookingStatus status);

    /**
     * Find bookings by user with filters
     */
    @Query("SELECT b FROM Booking b WHERE b.userId = :userId " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:checkInFrom IS NULL OR b.checkInDate >= :checkInFrom) " +
           "AND (:checkInTo IS NULL OR b.checkInDate <= :checkInTo) " +
           "ORDER BY b.createdAt DESC")
    @EntityGraph(attributePaths = "items")
    Page<Booking> findUserBookings(@Param("userId") Long userId,
                                  @Param("status") BookingStatus status,
                                  @Param("checkInFrom") LocalDate checkInFrom,
                                  @Param("checkInTo") LocalDate checkInTo,
                                  Pageable pageable);

    /**
     * Find bookings by campsite with filters (for merchants)
     */
    @Query("SELECT b FROM Booking b WHERE b.campsiteId = :campsiteId " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:checkInFrom IS NULL OR b.checkInDate >= :checkInFrom) " +
           "AND (:checkInTo IS NULL OR b.checkInDate <= :checkInTo) " +
           "ORDER BY b.createdAt DESC")
    @EntityGraph(attributePaths = "items")
    Page<Booking> findCampsiteBookings(@Param("campsiteId") Long campsiteId,
                                      @Param("status") BookingStatus status,
                                      @Param("checkInFrom") LocalDate checkInFrom,
                                      @Param("checkInTo") LocalDate checkInTo,
                                      Pageable pageable);

    /**
     * Check if spot is booked for date range
     */
    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE " +
           "b.spotId = :spotId AND b.status IN ('CONFIRMED', 'COMPLETED') " +
           "AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)")
    boolean isSpotBookedForDateRange(@Param("spotId") String spotId,
                                   @Param("checkIn") LocalDate checkIn,
                                   @Param("checkOut") LocalDate checkOut);

    /**
     * Find expired cart bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'IN_CART' AND b.expiresAt < :now")
    List<Booking> findExpiredCartBookings(@Param("now") LocalDateTime now);

    /**
     * Find expired payment pending bookings
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'PAYMENT_PENDING' AND b.expiresAt < :now")
    List<Booking> findExpiredPaymentBookings(@Param("now") LocalDateTime now);

    /**
     * Find booking by invoice number
     */
    Optional<Booking> findByInvoiceNumber(String invoiceNumber);

    /**
     * Find booking by id with items eagerly loaded
     */
    @EntityGraph(attributePaths = "items")
    Optional<Booking> findById(Long id);

    /**
     * Find user's cart booking by item id (for remove operations).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Booking b JOIN b.items i WHERE b.userId = :userId AND b.status = 'IN_CART' AND i.id = :itemId")
    @EntityGraph(attributePaths = "items")
    Optional<Booking> findUserCartByItemId(@Param("userId") Long userId, @Param("itemId") Long itemId);

    /**
     * Find bookings to complete (after checkout date)
     */
    @Query("SELECT b FROM Booking b WHERE b.status = 'CONFIRMED' AND b.checkOutDate < :date")
    List<Booking> findBookingsToComplete(@Param("date") LocalDate date);

    /**
     * Count bookings by status for a campsite
     */
    long countByCampsiteIdAndStatus(Long campsiteId, BookingStatus status);

    /**
     * Count bookings by status for a user
     */
    long countByUserIdAndStatus(Long userId, BookingStatus status);

    /**
     * Clean up expired cart bookings
     */
    @Modifying
    @Query("DELETE FROM Booking b WHERE b.status = 'IN_CART' AND b.expiresAt < :cutoffTime")
    int deleteExpiredCartBookings(@Param("cutoffTime") LocalDateTime cutoffTime);

    /**
     * Find bookings for specific dates (for availability checking)
     */
    @Query("SELECT b FROM Booking b WHERE " +
           "b.campsiteId = :campsiteId AND b.status IN ('CONFIRMED', 'COMPLETED') " +
           "AND NOT (b.checkOutDate <= :checkIn OR b.checkInDate >= :checkOut)")
    List<Booking> findBookingsForDateRange(@Param("campsiteId") Long campsiteId,
                                         @Param("checkIn") LocalDate checkIn,
                                         @Param("checkOut") LocalDate checkOut);

    /**
     * Get revenue statistics for a campsite
     */
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE " +
           "b.campsiteId = :campsiteId AND b.status IN ('CONFIRMED', 'COMPLETED') " +
           "AND b.confirmedAt >= :fromDate AND b.confirmedAt <= :toDate")
    Optional<java.math.BigDecimal> getRevenueForPeriod(@Param("campsiteId") Long campsiteId,
                                                       @Param("fromDate") LocalDateTime fromDate,
                                                       @Param("toDate") LocalDateTime toDate);
}
