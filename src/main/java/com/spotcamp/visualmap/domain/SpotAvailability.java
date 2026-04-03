package com.spotcamp.visualmap.domain;

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
 * Entity representing spot availability cache for real-time availability queries
 */
@Entity
@Table(name = "spot_availability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"campsite_id", "spot_id", "date"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "spot_id", nullable = false, length = 100)
    private String spotId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "is_available", nullable = false)
    private boolean available = true;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "price_override", precision = 12, scale = 2)
    private BigDecimal priceOverride;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    /**
     * Marks spot as unavailable due to booking
     */
    public void markAsBooked(Long bookingId) {
        this.available = false;
        this.bookingId = bookingId;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Marks spot as available (booking cancelled/released)
     */
    public void markAsAvailable() {
        this.available = true;
        this.bookingId = null;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Sets price override for this specific date
     */
    public void setPriceOverride(BigDecimal price) {
        this.priceOverride = price;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if spot has a custom price for this date
     */
    public boolean hasCustomPrice() {
        return priceOverride != null;
    }

    /**
     * Creates a new availability record for a spot on a date
     */
    public static SpotAvailability createAvailable(Long campsiteId, String spotId, LocalDate date) {
        return SpotAvailability.builder()
                .campsiteId(campsiteId)
                .spotId(spotId)
                .date(date)
                .available(true)
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    /**
     * Creates a booked availability record
     */
    public static SpotAvailability createBooked(Long campsiteId, String spotId, LocalDate date, Long bookingId) {
        return SpotAvailability.builder()
                .campsiteId(campsiteId)
                .spotId(spotId)
                .date(date)
                .available(false)
                .bookingId(bookingId)
                .lastUpdated(LocalDateTime.now())
                .build();
    }
}
