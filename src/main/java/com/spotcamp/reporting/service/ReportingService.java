package com.spotcamp.reporting.service;

import com.spotcamp.campsite.domain.Campsite;
import com.spotcamp.campsite.repository.CampsiteRepository;
import com.spotcamp.reporting.domain.BookingReport;
import com.spotcamp.reporting.domain.RevenueReport;
import com.spotcamp.reporting.repository.BookingReportRepository;
import com.spotcamp.reporting.repository.RevenueReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.*;

/**
 * Service for reporting operations and CQRS read models
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReportingService {

    private final BookingReportRepository bookingReportRepository;
    private final RevenueReportRepository revenueReportRepository;
    private final CampsiteRepository campsiteRepository;

    /**
     * Get booking reports with filters
     */
    public Page<BookingReport> getBookingReports(Long campsiteId, Long businessId, String status, 
                                                LocalDate fromDate, LocalDate toDate, Pageable pageable) {
        if (campsiteId != null) {
            return bookingReportRepository.findByCampsiteWithFilters(campsiteId, status, fromDate, toDate, pageable);
        }
        if (businessId != null) {
            List<Long> campsiteIds = campsiteRepository.findByOwnerId(businessId, Pageable.unpaged())
                    .stream()
                    .map(Campsite::getId)
                    .toList();
            if (campsiteIds.isEmpty()) {
                return Page.empty(pageable);
            }
            return bookingReportRepository.findByCampsiteIdsWithFilters(campsiteIds, status, fromDate, toDate, pageable);
        }
        return bookingReportRepository.findAllWithFilters(status, fromDate, toDate, pageable);
    }

    /**
     * Get revenue summary for dashboard
     */
    public Map<String, Object> getRevenueSummary(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> summary = new HashMap<>();

        Object[] result = bookingReportRepository.getRevenueSummary(campsiteId, fromDate, toDate);
        if (result == null || result.length < 4) {
            summary.put("totalRevenue", BigDecimal.ZERO);
            summary.put("confirmedBookings", 0);
            summary.put("cancelledBookings", 0);
            summary.put("totalBookings", 0);
        } else {
            summary.put("totalRevenue", result[0] != null ? result[0] : BigDecimal.ZERO);
            summary.put("confirmedBookings", result[1] != null ? result[1] : 0);
            summary.put("cancelledBookings", result[2] != null ? result[2] : 0);
            summary.put("totalBookings", result[3] != null ? result[3] : 0);
        }
        
        // Calculate conversion rate
        Integer total = ((Number) summary.get("totalBookings")).intValue();
        Integer confirmed = ((Number) summary.get("confirmedBookings")).intValue();
        if (total > 0) {
            BigDecimal conversionRate = BigDecimal.valueOf(confirmed * 100.0 / total)
                    .setScale(2, RoundingMode.HALF_UP);
            summary.put("conversionRate", conversionRate);
        } else {
            summary.put("conversionRate", BigDecimal.ZERO);
        }
        
        return summary;
    }

    /**
     * Get monthly booking trends
     */
    public List<Map<String, Object>> getMonthlyTrends(Long campsiteId, Integer year) {
        List<Object[]> results = bookingReportRepository.getMonthlyTrends(campsiteId, year);
        
        List<Map<String, Object>> trends = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("month", result[0]);
            trend.put("bookingCount", result[1]);
            trend.put("revenue", result[2]);
            trends.add(trend);
        }
        
        return trends;
    }

    /**
     * Get top performing spots
     */
    public List<Map<String, Object>> getTopSpots(Long campsiteId, LocalDate fromDate, LocalDate toDate, int limit) {
        List<Object[]> results = bookingReportRepository.getTopSpots(campsiteId, fromDate, toDate);
        
        List<Map<String, Object>> topSpots = new ArrayList<>();
        int count = 0;
        for (Object[] result : results) {
            if (count >= limit) break;
            
            Map<String, Object> spot = new HashMap<>();
            spot.put("spotId", result[0]);
            spot.put("spotName", result[1]);
            spot.put("bookingCount", result[2]);
            spot.put("revenue", result[3]);
            topSpots.add(spot);
            count++;
        }
        
        return topSpots;
    }

    /**
     * Get customer analytics
     */
    public Map<String, Object> getCustomerAnalytics(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> analytics = new HashMap<>();

        Object[] result = bookingReportRepository.getCustomerAnalytics(campsiteId, fromDate, toDate);
        if (result == null || result.length < 4) {
            analytics.put("uniqueCustomers", 0);
            analytics.put("averageBookingValue", BigDecimal.ZERO);
            analytics.put("averageAdvanceDays", BigDecimal.ZERO);
            analytics.put("weekendBookings", 0);
        } else {
            analytics.put("uniqueCustomers", result[0] != null ? result[0] : 0);
            analytics.put("averageBookingValue", result[1] != null ? result[1] : BigDecimal.ZERO);
            analytics.put("averageAdvanceDays", result[2] != null ? result[2] : BigDecimal.ZERO);
            analytics.put("weekendBookings", result[3] != null ? result[3] : 0);
        }
        
        return analytics;
    }

    /**
     * Get seasonal performance
     */
    public List<Map<String, Object>> getSeasonalPerformance(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = bookingReportRepository.getSeasonalPerformance(campsiteId, fromDate, toDate);
        
        List<Map<String, Object>> performance = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> season = new HashMap<>();
            season.put("season", result[0]);
            season.put("bookingCount", result[1]);
            season.put("revenue", result[2]);
            season.put("averageValue", result[3]);
            performance.add(season);
        }
        
        return performance;
    }

    /**
     * Get recent bookings for real-time dashboard
     */
    public List<BookingReport> getRecentBookings(Long campsiteId, int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return bookingReportRepository.findRecentBookings(campsiteId, since);
    }

    /**
     * Get booking status distribution
     */
    public Map<String, Long> getBookingStatusDistribution(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = bookingReportRepository.getBookingStatusDistribution(campsiteId, fromDate, toDate);
        
        Map<String, Long> distribution = new HashMap<>();
        for (Object[] result : results) {
            distribution.put((String) result[0], (Long) result[1]);
        }
        
        return distribution;
    }

    /**
     * Get occupancy calendar data
     */
    public List<Map<String, Object>> getOccupancyCalendar(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        List<Object[]> results = bookingReportRepository.getOccupancyCalendar(campsiteId, fromDate, toDate);
        
        List<Map<String, Object>> calendar = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> day = new HashMap<>();
            day.put("date", result[0]);
            day.put("bookings", result[1]);
            calendar.add(day);
        }
        
        return calendar;
    }

    /**
     * Get revenue trends from aggregated data
     */
    public List<RevenueReport> getRevenueTrends(Long campsiteId, String periodType, 
                                               LocalDate fromDate, LocalDate toDate) {
        return switch (periodType.toLowerCase()) {
            case "monthly" -> revenueReportRepository.getMonthlyTrends(campsiteId, fromDate, toDate);
            case "yearly" -> revenueReportRepository.getYearlyComparison(campsiteId);
            case "system" -> revenueReportRepository.getSystemWideTrends(periodType, fromDate, toDate);
            default -> Collections.emptyList();
        };
    }

    /**
     * Get occupancy trends
     */
    public List<Map<String, Object>> getOccupancyTrends(Long campsiteId, LocalDate fromDate) {
        List<Object[]> results = revenueReportRepository.getOccupancyTrends(campsiteId, fromDate);
        
        List<Map<String, Object>> trends = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> trend = new HashMap<>();
            trend.put("period", result[0]);
            trend.put("occupancyRate", result[1]);
            trends.add(trend);
        }
        
        return trends;
    }

    /**
     * Get customer retention metrics
     */
    public List<Map<String, Object>> getCustomerRetentionMetrics(Long campsiteId, LocalDate fromDate) {
        List<Object[]> results = revenueReportRepository.getCustomerRetentionMetrics(campsiteId, fromDate);
        
        List<Map<String, Object>> metrics = new ArrayList<>();
        for (Object[] result : results) {
            Map<String, Object> metric = new HashMap<>();
            metric.put("period", result[0]);
            metric.put("newCustomers", result[1]);
            metric.put("returningCustomers", result[2]);
            metric.put("uniqueCustomers", result[3]);
            
            // Calculate retention rate
            Integer returning = (Integer) result[2];
            Integer total = (Integer) result[3];
            if (total > 0) {
                BigDecimal retentionRate = BigDecimal.valueOf(returning * 100.0 / total)
                        .setScale(2, RoundingMode.HALF_UP);
                metric.put("retentionRate", retentionRate);
            } else {
                metric.put("retentionRate", BigDecimal.ZERO);
            }
            
            metrics.add(metric);
        }
        
        return metrics;
    }

    /**
     * Get weekend vs weekday performance
     */
    public Map<String, Object> getWeekendVsWeekdayPerformance(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> performance = new HashMap<>();

        Object[] result = revenueReportRepository.getWeekendVsWeekdayPerformance(campsiteId, fromDate, toDate);
        if (result == null || result.length < 4) {
            performance.put("weekendRevenue", BigDecimal.ZERO);
            performance.put("weekdayRevenue", BigDecimal.ZERO);
            performance.put("weekendBookings", 0);
            performance.put("weekdayBookings", 0);
        } else {
            performance.put("weekendRevenue", result[0] != null ? result[0] : BigDecimal.ZERO);
            performance.put("weekdayRevenue", result[1] != null ? result[1] : BigDecimal.ZERO);
            performance.put("weekendBookings", result[2] != null ? result[2] : 0);
            performance.put("weekdayBookings", result[3] != null ? result[3] : 0);
        }
        
        return performance;
    }

    /**
     * Get revenue breakdown by product type
     */
    public Map<String, Object> getRevenueByProductType(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> breakdown = new HashMap<>();

        Object[] result = revenueReportRepository.getRevenueByProductType(campsiteId, fromDate, toDate);
        if (result == null || result.length < 2) {
            breakdown.put("spotRevenue", BigDecimal.ZERO);
            breakdown.put("productRevenue", BigDecimal.ZERO);
        } else {
            breakdown.put("spotRevenue", result[0] != null ? result[0] : BigDecimal.ZERO);
            breakdown.put("productRevenue", result[1] != null ? result[1] : BigDecimal.ZERO);
        }
        
        BigDecimal spotRevenue = (BigDecimal) breakdown.get("spotRevenue");
        BigDecimal productRevenue = (BigDecimal) breakdown.get("productRevenue");
        BigDecimal totalRevenue = spotRevenue.add(productRevenue);
        breakdown.put("totalRevenue", totalRevenue);
        
        // Calculate percentages
        if (totalRevenue.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal spotPercentage = spotRevenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
            BigDecimal productPercentage = productRevenue.multiply(BigDecimal.valueOf(100))
                    .divide(totalRevenue, 2, RoundingMode.HALF_UP);
            
            breakdown.put("spotPercentage", spotPercentage);
            breakdown.put("productPercentage", productPercentage);
        } else {
            breakdown.put("spotPercentage", BigDecimal.ZERO);
            breakdown.put("productPercentage", BigDecimal.ZERO);
        }
        
        return breakdown;
    }

    /**
     * System-wide analytics (admin only)
     */
    public Map<String, Object> getSystemWideAnalytics(LocalDate fromDate, LocalDate toDate) {
        Object[] result = bookingReportRepository.getSystemWideAnalytics(fromDate, toDate);
        
        Map<String, Object> analytics = new HashMap<>();
        analytics.put("activeCampsites", result[0] != null ? result[0] : 0);
        analytics.put("totalUsers", result[1] != null ? result[1] : 0);
        analytics.put("totalRevenue", result[2] != null ? result[2] : BigDecimal.ZERO);
        analytics.put("totalBookings", result[3] != null ? result[3] : 0);
        
        return analytics;
    }

    /**
     * Get campsite performance ranking
     */
    public List<Map<String, Object>> getCampsiteRanking(LocalDate fromDate, LocalDate toDate, int limit) {
        List<Object[]> results = bookingReportRepository.getCampsiteRanking(fromDate, toDate);
        
        List<Map<String, Object>> ranking = new ArrayList<>();
        int rank = 1;
        for (Object[] result : results) {
            if (rank > limit) break;
            
            Map<String, Object> campsite = new HashMap<>();
            campsite.put("rank", rank);
            campsite.put("campsiteId", result[0]);
            campsite.put("campsiteName", result[1]);
            campsite.put("revenue", result[2]);
            campsite.put("bookings", result[3]);
            ranking.add(campsite);
            rank++;
        }
        
        return ranking;
    }

    /**
     * Generate comprehensive dashboard data
     */
    public Map<String, Object> getDashboardData(Long campsiteId, LocalDate fromDate, LocalDate toDate) {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Revenue summary
        dashboard.put("revenueSummary", getRevenueSummary(campsiteId, fromDate, toDate));
        
        // Customer analytics
        dashboard.put("customerAnalytics", getCustomerAnalytics(campsiteId, fromDate, toDate));
        
        // Top spots (limit to 5)
        dashboard.put("topSpots", getTopSpots(campsiteId, fromDate, toDate, 5));
        
        // Booking status distribution
        dashboard.put("statusDistribution", getBookingStatusDistribution(campsiteId, fromDate, toDate));
        
        // Recent bookings (last 24 hours)
        dashboard.put("recentBookings", getRecentBookings(campsiteId, 24));
        
        // Weekend vs weekday performance
        dashboard.put("weekendVsWeekday", getWeekendVsWeekdayPerformance(campsiteId, fromDate, toDate));
        
        // Revenue breakdown
        dashboard.put("revenueBreakdown", getRevenueByProductType(campsiteId, fromDate, toDate));
        
        return dashboard;
    }
}
