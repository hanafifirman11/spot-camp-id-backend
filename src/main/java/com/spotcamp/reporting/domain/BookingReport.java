package com.spotcamp.reporting.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Read model for booking reports (CQRS pattern)
 * Optimized for query performance
 */
@Entity
@Table(name = "booking_reports", indexes = {
        @Index(name = "idx_booking_report_campsite_date", columnList = "campsite_id, booking_date"),
        @Index(name = "idx_booking_report_status", columnList = "status"),
        @Index(name = "idx_booking_report_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingReport {

    @Id
    private Long bookingId; // Same as booking ID for easy correlation

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "campsite_name", length = 200)
    private String campsiteName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "spot_id", length = 100)
    private String spotId;

    @Column(name = "spot_name", length = 200)
    private String spotName;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "nights", nullable = false)
    private Integer nights;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "total_amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal totalAmount;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "booking_month", nullable = false)
    private String bookingMonth; // YYYY-MM format for monthly reports

    @Column(name = "booking_year", nullable = false)
    private Integer bookingYear;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "total_items", nullable = false)
    private Integer totalItems;

    @Column(name = "has_products", nullable = false)
    private Boolean hasProducts; // True if booking includes product purchases

    @Column(name = "revenue_month", nullable = false)
    private String revenueMonth; // Month when revenue was recognized (confirmed_at)

    // Derived fields for reporting
    @Column(name = "is_weekend_booking", nullable = false)
    private Boolean isWeekendBooking;

    @Column(name = "advance_days", nullable = false)
    private Integer advanceDays; // Days between booking and check-in

    @Column(name = "season", length = 20)
    private String season; // HIGH, MEDIUM, LOW based on date

    /**
     * Updates revenue month based on confirmation date
     */
    public void updateRevenueMonth() {
        if (confirmedAt != null) {
            this.revenueMonth = String.format("%04d-%02d", 
                    confirmedAt.getYear(), 
                    confirmedAt.getMonthValue());
        }
    }

    /**
     * Calculates if check-in is on weekend
     */
    public void calculateWeekendBooking() {
        if (checkInDate != null) {
            int dayOfWeek = checkInDate.getDayOfWeek().getValue();
            this.isWeekendBooking = (dayOfWeek == 6 || dayOfWeek == 7); // Saturday or Sunday
        }
    }

    /**
     * Calculates advance booking days
     */
    public void calculateAdvanceDays() {
        if (createdAt != null && checkInDate != null) {
            this.advanceDays = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    createdAt.toLocalDate(), checkInDate);
        }
    }

    /**
     * Determines season based on check-in month
     */
    public void calculateSeason() {
        if (checkInDate != null) {
            int month = checkInDate.getMonthValue();
            if (month >= 6 && month <= 8) {
                this.season = "HIGH"; // Summer
            } else if (month >= 3 && month <= 5 || month >= 9 && month <= 11) {
                this.season = "MEDIUM"; // Spring/Fall
            } else {
                this.season = "LOW"; // Winter
            }
        }
    }
}