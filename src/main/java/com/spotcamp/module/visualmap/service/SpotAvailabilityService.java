package com.spotcamp.module.visualmap.service;

import com.spotcamp.module.visualmap.dto.SpotAvailabilityDTO;
import com.spotcamp.module.booking.entity.InventoryLock;
import com.spotcamp.module.booking.entity.InventoryLock.LockType;
import com.spotcamp.module.booking.repository.BookingRepository;
import com.spotcamp.module.booking.repository.InventoryLockRepository;
import com.spotcamp.module.visualmap.entity.SpotDefinition;
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
    public List<SpotAvailabilityDTO> getSpotAvailability(Long campsiteId, LocalDate checkIn, 
                                                        LocalDate checkOut, List<SpotDefinition> spots) {
        
        log.debug("Checking availability for campsite {} from {} to {}", campsiteId, checkIn, checkOut);
        
        return spots.stream()
                .map(spot -> checkSpotAvailability(spot.getId(), checkIn, checkOut))
                .collect(Collectors.toList());
    }

    /**
     * Check availability for a specific spot
     */
    public SpotAvailabilityDTO checkSpotAvailability(String spotId, LocalDate checkIn, LocalDate checkOut) {
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

        SpotAvailabilityDTO.SpotStatus status;
        if (hasBooking || hasConfirmedLock) {
            status = SpotAvailabilityDTO.SpotStatus.BOOKED;
        } else if (lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now())) {
            status = SpotAvailabilityDTO.SpotStatus.LOCKED;
        } else {
            status = SpotAvailabilityDTO.SpotStatus.AVAILABLE;
        }
        
        return SpotAvailabilityDTO.builder()
                .spotId(spotId)
                .status(status)
                .lockedUntil(lockedUntil)
                .build();
    }

    /**
     * Check if a specific spot is available for booking
     */
    public boolean isSpotAvailable(String spotId, LocalDate checkIn, LocalDate checkOut) {
        SpotAvailabilityDTO availability = checkSpotAvailability(spotId, checkIn, checkOut);
        return availability.getStatus() == SpotAvailabilityDTO.SpotStatus.AVAILABLE;
    }
}
