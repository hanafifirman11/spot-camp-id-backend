package com.spotcamp.module.reporting.repository;

import com.spotcamp.module.reporting.entity.BookingReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for BookingReport read model
 */
@Repository
public interface BookingReportRepository extends JpaRepository<BookingReport, Long> {

    /**
     * Find booking reports by campsite with filters
     */
    @Query("SELECT br FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND (:status IS NULL OR br.status = :status) " +
           "AND (:fromDate IS NULL OR br.bookingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR br.bookingDate <= :toDate) " +
           "ORDER BY br.bookingDate DESC")
    Page<BookingReport> findByCampsiteWithFilters(@Param("campsiteId") Long campsiteId,
                                                 @Param("status") String status,
                                                 @Param("fromDate") LocalDate fromDate,
                                                 @Param("toDate") LocalDate toDate,
                                                 Pageable pageable);

    /**
     * Find all booking reports with filters (admin view)
     */
    @Query("SELECT br FROM BookingReport br WHERE " +
           "(:status IS NULL OR br.status = :status) " +
           "AND (:fromDate IS NULL OR br.bookingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR br.bookingDate <= :toDate) " +
           "ORDER BY br.bookingDate DESC")
    Page<BookingReport> findAllWithFilters(@Param("status") String status,
                                         @Param("fromDate") LocalDate fromDate,
                                         @Param("toDate") LocalDate toDate,
                                         Pageable pageable);

    /**
     * Find booking reports by campsite IDs with filters (admin business view).
     */
    @Query("SELECT br FROM BookingReport br WHERE " +
           "br.campsiteId IN :campsiteIds " +
           "AND (:status IS NULL OR br.status = :status) " +
           "AND (:fromDate IS NULL OR br.bookingDate >= :fromDate) " +
           "AND (:toDate IS NULL OR br.bookingDate <= :toDate) " +
           "ORDER BY br.bookingDate DESC")
    Page<BookingReport> findByCampsiteIdsWithFilters(@Param("campsiteIds") List<Long> campsiteIds,
                                                    @Param("status") String status,
                                                    @Param("fromDate") LocalDate fromDate,
                                                    @Param("toDate") LocalDate toDate,
                                                    Pageable pageable);

    /**
     * Get revenue summary for campsite in date range
     */
    @Query("SELECT " +
           "SUM(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN br.totalAmount ELSE 0 END) as totalRevenue, " +
           "COUNT(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN 1 END) as confirmedBookings, " +
           "COUNT(CASE WHEN br.status = 'CANCELLED' THEN 1 END) as cancelledBookings, " +
           "COUNT(*) as totalBookings " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingDate BETWEEN :fromDate AND :toDate")
    Object[] getRevenueSummary(@Param("campsiteId") Long campsiteId,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate);

    /**
     * Get monthly booking trends
     */
    @Query("SELECT br.bookingMonth, " +
           "COUNT(*) as bookingCount, " +
           "SUM(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN br.totalAmount ELSE 0 END) as revenue " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingYear = :year " +
           "GROUP BY br.bookingMonth " +
           "ORDER BY br.bookingMonth")
    List<Object[]> getMonthlyTrends(@Param("campsiteId") Long campsiteId,
                                   @Param("year") Integer year);

    /**
     * Get top performing spots
     */
    @Query("SELECT br.spotId, br.spotName, " +
           "COUNT(*) as bookingCount, " +
           "SUM(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN br.totalAmount ELSE 0 END) as revenue " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingDate BETWEEN :fromDate AND :toDate " +
           "AND (br.status = 'CONFIRMED' OR br.status = 'COMPLETED') " +
           "GROUP BY br.spotId, br.spotName " +
           "ORDER BY revenue DESC")
    List<Object[]> getTopSpots(@Param("campsiteId") Long campsiteId,
                              @Param("fromDate") LocalDate fromDate,
                              @Param("toDate") LocalDate toDate);

    /**
     * Get customer analytics
     */
    @Query("SELECT " +
           "COUNT(DISTINCT br.userId) as uniqueCustomers, " +
           "AVG(br.totalAmount) as averageBookingValue, " +
           "AVG(br.advanceDays) as averageAdvanceDays, " +
           "SUM(CASE WHEN br.isWeekendBooking = true THEN 1 ELSE 0 END) as weekendBookings " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingDate BETWEEN :fromDate AND :toDate " +
           "AND (br.status = 'CONFIRMED' OR br.status = 'COMPLETED')")
    Object[] getCustomerAnalytics(@Param("campsiteId") Long campsiteId,
                                 @Param("fromDate") LocalDate fromDate,
                                 @Param("toDate") LocalDate toDate);

    /**
     * Get seasonal performance
     */
    @Query("SELECT br.season, " +
           "COUNT(*) as bookingCount, " +
           "SUM(br.totalAmount) as revenue, " +
           "AVG(br.totalAmount) as averageValue " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingDate BETWEEN :fromDate AND :toDate " +
           "AND (br.status = 'CONFIRMED' OR br.status = 'COMPLETED') " +
           "GROUP BY br.season " +
           "ORDER BY revenue DESC")
    List<Object[]> getSeasonalPerformance(@Param("campsiteId") Long campsiteId,
                                         @Param("fromDate") LocalDate fromDate,
                                         @Param("toDate") LocalDate toDate);

    /**
     * Find recent bookings for real-time dashboard
     */
    @Query("SELECT br FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.createdAt >= :since " +
           "ORDER BY br.createdAt DESC")
    List<BookingReport> findRecentBookings(@Param("campsiteId") Long campsiteId,
                                          @Param("since") LocalDateTime since);

    /**
     * Get booking status distribution
     */
    @Query("SELECT br.status, COUNT(*) " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.bookingDate BETWEEN :fromDate AND :toDate " +
           "GROUP BY br.status")
    List<Object[]> getBookingStatusDistribution(@Param("campsiteId") Long campsiteId,
                                               @Param("fromDate") LocalDate fromDate,
                                               @Param("toDate") LocalDate toDate);

    /**
     * Get occupancy data for calendar view
     */
    @Query("SELECT br.checkInDate, COUNT(*) as bookings " +
           "FROM BookingReport br WHERE " +
           "br.campsiteId = :campsiteId " +
           "AND br.checkInDate BETWEEN :fromDate AND :toDate " +
           "AND (br.status = 'CONFIRMED' OR br.status = 'COMPLETED') " +
           "GROUP BY br.checkInDate " +
           "ORDER BY br.checkInDate")
    List<Object[]> getOccupancyCalendar(@Param("campsiteId") Long campsiteId,
                                       @Param("fromDate") LocalDate fromDate,
                                       @Param("toDate") LocalDate toDate);

    /**
     * System-wide analytics (admin only)
     */
    @Query("SELECT " +
           "COUNT(DISTINCT br.campsiteId) as activeCampsites, " +
           "COUNT(DISTINCT br.userId) as totalUsers, " +
           "SUM(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN br.totalAmount ELSE 0 END) as totalRevenue, " +
           "COUNT(*) as totalBookings " +
           "FROM BookingReport br WHERE " +
           "br.bookingDate BETWEEN :fromDate AND :toDate")
    Object[] getSystemWideAnalytics(@Param("fromDate") LocalDate fromDate,
                                   @Param("toDate") LocalDate toDate);

    /**
     * Get campsite performance ranking
     */
    @Query("SELECT br.campsiteId, br.campsiteName, " +
           "SUM(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN br.totalAmount ELSE 0 END) as revenue, " +
           "COUNT(CASE WHEN br.status = 'CONFIRMED' OR br.status = 'COMPLETED' THEN 1 END) as bookings " +
           "FROM BookingReport br WHERE " +
           "br.bookingDate BETWEEN :fromDate AND :toDate " +
           "GROUP BY br.campsiteId, br.campsiteName " +
           "ORDER BY revenue DESC")
    List<Object[]> getCampsiteRanking(@Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);
}
