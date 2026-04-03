package com.spotcamp.module.reporting.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregated revenue report (CQRS read model)
 * Pre-calculated for performance
 */
@Entity
@Table(name = "revenue_reports", indexes = {
        @Index(name = "idx_revenue_campsite_period", columnList = "campsite_id, period_type, period_key"),
        @Index(name = "idx_revenue_period", columnList = "period_type, period_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevenueReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id")
    private Long campsiteId; // Null for system-wide reports

    @Column(name = "campsite_name", length = 200)
    private String campsiteName;

    @Column(name = "period_type", length = 20, nullable = false)
    private String periodType; // DAILY, WEEKLY, MONTHLY, YEARLY

    @Column(name = "period_key", length = 20, nullable = false)
    private String periodKey; // 2024-01-15, 2024-W03, 2024-01, 2024

    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    // Revenue metrics
    @Column(name = "total_revenue", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalRevenue;

    @Column(name = "spot_revenue", precision = 12, scale = 2, nullable = false)
    private BigDecimal spotRevenue;

    @Column(name = "product_revenue", precision = 12, scale = 2, nullable = false)
    private BigDecimal productRevenue;

    // Booking metrics
    @Column(name = "total_bookings", nullable = false)
    private Integer totalBookings;

    @Column(name = "confirmed_bookings", nullable = false)
    private Integer confirmedBookings;

    @Column(name = "cancelled_bookings", nullable = false)
    private Integer cancelledBookings;

    @Column(name = "total_nights", nullable = false)
    private Integer totalNights;

    @Column(name = "average_booking_value", precision = 8, scale = 2)
    private BigDecimal averageBookingValue;

    @Column(name = "occupancy_rate", precision = 5, scale = 2)
    private BigDecimal occupancyRate; // Percentage

    // Customer metrics
    @Column(name = "unique_customers", nullable = false)
    private Integer uniqueCustomers;

    @Column(name = "new_customers", nullable = false)
    private Integer newCustomers;

    @Column(name = "returning_customers", nullable = false)
    private Integer returningCustomers;

    // Seasonal metrics
    @Column(name = "weekend_bookings", nullable = false)
    private Integer weekendBookings;

    @Column(name = "weekday_bookings", nullable = false)
    private Integer weekdayBookings;

    @Column(name = "weekend_revenue", precision = 12, scale = 2)
    private BigDecimal weekendRevenue;

    @Column(name = "weekday_revenue", precision = 12, scale = 2)
    private BigDecimal weekdayRevenue;

    // Performance metrics
    @Column(name = "advance_booking_days", precision = 6, scale = 2)
    private BigDecimal averageAdvanceBookingDays;

    @Column(name = "last_updated", nullable = false)
    private java.time.LocalDateTime lastUpdated;

    /**
     * Calculates derived metrics
     */
    public void calculateDerivedMetrics() {
        // Average booking value
        if (confirmedBookings > 0) {
            this.averageBookingValue = totalRevenue.divide(
                    BigDecimal.valueOf(confirmedBookings), 
                    2, 
                    java.math.RoundingMode.HALF_UP
            );
        } else {
            this.averageBookingValue = BigDecimal.ZERO;
        }

        // Weekend vs weekday ratios
        int totalDayBookings = weekendBookings + weekdayBookings;
        if (totalDayBookings > 0) {
            BigDecimal weekendPercentage = BigDecimal.valueOf(weekendBookings * 100.0 / totalDayBookings);
            // Store as additional field if needed
        }
    }

    /**
     * Gets formatted period display
     */
    public String getPeriodDisplay() {
        return switch (periodType) {
            case "DAILY" -> periodStart.toString();
            case "WEEKLY" -> "Week " + periodKey.substring(6); // Extract week number
            case "MONTHLY" -> periodStart.getYear() + "/" + String.format("%02d", periodStart.getMonthValue());
            case "YEARLY" -> String.valueOf(periodStart.getYear());
            default -> periodKey;
        };
    }

    /**
     * Checks if this is a system-wide report
     */
    public boolean isSystemWideReport() {
        return campsiteId == null;
    }
}