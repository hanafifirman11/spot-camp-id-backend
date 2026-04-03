package com.spotcamp.visualmap.service;

import com.spotcamp.visualmap.api.dto.SpotAvailabilityDto;
import com.spotcamp.booking.domain.InventoryLock;
import com.spotcamp.booking.domain.InventoryLock.LockType;
import com.spotcamp.booking.repository.BookingRepository;
import com.spotcamp.booking.repository.InventoryLockRepository;
import com.spotcamp.visualmap.domain.SpotDefinition;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for checking spot availability
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SpotAvailabilityService {

    private final BookingRepository bookingRepository;
    private final InventoryLockRepository inventoryLockRepository;

    /**
     * Get availability for all spots in a date range
     */
    public List<SpotAvailabilityDto> getSpotAvailability(Long campsiteId, LocalDate checkIn, 
                                                        LocalDate checkOut, List<SpotDefinition> spots) {
        
        log.debug("Checking availability for campsite {} from {} to {}", campsiteId, checkIn, checkOut);
        
        return spots.stream()
                .map(spot -> checkSpotAvailability(spot.getId(), checkIn, checkOut))
                .collect(Collectors.toList());
    }

    /**
     * Check availability for a specific spot
     */
    public SpotAvailabilityDto checkSpotAvailability(String spotId, LocalDate checkIn, LocalDate checkOut) {
        boolean hasBooking = bookingRepository.isSpotBookedForDateRange(spotId, checkIn, checkOut);

        List<InventoryLock> confirmedLocks = inventoryLockRepository.findSpotLocksForDateRange(
                spotId, LockType.CONFIRMED, checkIn, checkOut);
        boolean hasConfirmedLock = confirmedLocks != null && !confirmedLocks.isEmpty();

        List<InventoryLock> cartLocks = inventoryLockRepository.findSpotLocksForDateRange(
                spotId, LockType.CART, checkIn, checkOut);
        LocalDateTime lockedUntil = cartLocks.stream()
                .map(InventoryLock::getExpiresAt)
                .filter(expiresAt -> expiresAt != null && expiresAt.isAfter(LocalDateTime.now()))
                .max(Comparator.naturalOrder())
                .orElse(null);

        SpotAvailabilityDto.SpotStatus status;
        if (hasBooking || hasConfirmedLock) {
            status = SpotAvailabilityDto.SpotStatus.BOOKED;
        } else if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
            status = SpotAvailabilityDto.SpotStatus.LOCKED;
        } else {
            status = SpotAvailabilityDto.SpotStatus.AVAILABLE;
        }
        
        return SpotAvailabilityDto.builder()
                .spotId(spotId)
                .status(status)
                .lockedUntil(lockedUntil)
                .build();
    }

    /**
     * Check if a specific spot is available for booking
     */
    public boolean isSpotAvailable(String spotId, LocalDate checkIn, LocalDate checkOut) {
        SpotAvailabilityDto availability = checkSpotAvailability(spotId, checkIn, checkOut);
        return availability.getStatus() == SpotAvailabilityDto.SpotStatus.AVAILABLE;
    }
}
