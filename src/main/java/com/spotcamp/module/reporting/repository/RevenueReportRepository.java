package com.spotcamp.module.reporting.repository;

import com.spotcamp.module.reporting.entity.RevenueReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for RevenueReport aggregated data
 */
@Repository
public interface RevenueReportRepository extends JpaRepository<RevenueReport, Long> {

    /**
     * Find revenue report by campsite and period
     */
    Optional<RevenueReport> findByCampsiteIdAndPeriodTypeAndPeriodKey(
            Long campsiteId, String periodType, String periodKey);

    /**
     * Find system-wide revenue report by period
     */
    Optional<RevenueReport> findByCampsiteIdIsNullAndPeriodTypeAndPeriodKey(
            String periodType, String periodKey);

    /**
     * Get monthly revenue trends for campsite
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'MONTHLY' " +
           "AND rr.periodStart >= :fromDate " +
           "AND rr.periodEnd <= :toDate " +
           "ORDER BY rr.periodStart")
    List<RevenueReport> getMonthlyTrends(@Param("campsiteId") Long campsiteId,
                                        @Param("fromDate") LocalDate fromDate,
                                        @Param("toDate") LocalDate toDate);

    /**
     * Get daily revenue for specific month
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'DAILY' " +
           "AND rr.periodKey LIKE :monthPattern " +
           "ORDER BY rr.periodStart")
    List<RevenueReport> getDailyRevenueForMonth(@Param("campsiteId") Long campsiteId,
                                               @Param("monthPattern") String monthPattern);

    /**
     * Get yearly comparison data
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'YEARLY' " +
           "ORDER BY rr.periodStart DESC")
    List<RevenueReport> getYearlyComparison(@Param("campsiteId") Long campsiteId);

    /**
     * Get top performing campsites for period
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.periodType = :periodType " +
           "AND rr.periodKey = :periodKey " +
           "AND rr.campsiteId IS NOT NULL " +
           "ORDER BY rr.totalRevenue DESC")
    List<RevenueReport> getTopCampsitesForPeriod(@Param("periodType") String periodType,
                                                @Param("periodKey") String periodKey);

    /**
     * Get system-wide trends
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.campsiteId IS NULL " +
           "AND rr.periodType = :periodType " +
           "AND rr.periodStart >= :fromDate " +
           "AND rr.periodEnd <= :toDate " +
           "ORDER BY rr.periodStart")
    List<RevenueReport> getSystemWideTrends(@Param("periodType") String periodType,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

    /**
     * Get campsite performance comparison
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.campsiteId IN :campsiteIds " +
           "AND rr.periodType = :periodType " +
           "AND rr.periodKey = :periodKey")
    List<RevenueReport> getCampsiteComparison(@Param("campsiteIds") List<Long> campsiteIds,
                                             @Param("periodType") String periodType,
                                             @Param("periodKey") String periodKey);

    /**
     * Find reports that need updates (older than threshold)
     */
    @Query("SELECT rr FROM RevenueReport rr WHERE " +
           "rr.lastUpdated < :threshold " +
           "ORDER BY rr.lastUpdated")
    List<RevenueReport> findStaleReports(@Param("threshold") java.time.LocalDateTime threshold);

    /**
     * Get occupancy trends
     */
    @Query("SELECT rr.periodKey, rr.occupancyRate " +
           "FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'MONTHLY' " +
           "AND rr.periodStart >= :fromDate " +
           "ORDER BY rr.periodStart")
    List<Object[]> getOccupancyTrends(@Param("campsiteId") Long campsiteId,
                                     @Param("fromDate") LocalDate fromDate);

    /**
     * Get customer retention metrics
     */
    @Query("SELECT rr.periodKey, " +
           "rr.newCustomers, " +
           "rr.returningCustomers, " +
           "rr.uniqueCustomers " +
           "FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'MONTHLY' " +
           "AND rr.periodStart >= :fromDate " +
           "ORDER BY rr.periodStart")
    List<Object[]> getCustomerRetentionMetrics(@Param("campsiteId") Long campsiteId,
                                              @Param("fromDate") LocalDate fromDate);

    /**
     * Get weekend vs weekday performance
     */
    @Query("SELECT " +
           "SUM(rr.weekendRevenue) as totalWeekendRevenue, " +
           "SUM(rr.weekdayRevenue) as totalWeekdayRevenue, " +
           "SUM(rr.weekendBookings) as totalWeekendBookings, " +
           "SUM(rr.weekdayBookings) as totalWeekdayBookings " +
           "FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodType = 'MONTHLY' " +
           "AND rr.periodStart >= :fromDate " +
           "AND rr.periodEnd <= :toDate")
    Object[] getWeekendVsWeekdayPerformance(@Param("campsiteId") Long campsiteId,
                                           @Param("fromDate") LocalDate fromDate,
                                           @Param("toDate") LocalDate toDate);

    /**
     * Get revenue by product type
     */
    @Query("SELECT " +
           "SUM(rr.spotRevenue) as totalSpotRevenue, " +
           "SUM(rr.productRevenue) as totalProductRevenue " +
           "FROM RevenueReport rr WHERE " +
           "rr.campsiteId = :campsiteId " +
           "AND rr.periodStart >= :fromDate " +
           "AND rr.periodEnd <= :toDate")
    Object[] getRevenueByProductType(@Param("campsiteId") Long campsiteId,
                                    @Param("fromDate") LocalDate fromDate,
                                    @Param("toDate") LocalDate toDate);
}