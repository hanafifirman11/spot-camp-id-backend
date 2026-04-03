package com.spotcamp.module.booking.scheduler;

import com.spotcamp.module.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled task for cleaning up expired bookings and locks
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "spotcamp.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class BookingCleanupScheduler {

    private final BookingService bookingService;

    /**
     * Clean up expired carts and locks every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void cleanupExpiredData() {
        try {
            log.debug("Starting scheduled cleanup of expired bookings and locks");
            bookingService.cleanupExpiredData();
            log.debug("Completed scheduled cleanup of expired bookings and locks");
        } catch (Exception e) {
            log.error("Error during scheduled cleanup", e);
        }
    }
}