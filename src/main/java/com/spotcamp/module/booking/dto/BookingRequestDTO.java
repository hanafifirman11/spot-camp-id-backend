package com.spotcamp.module.booking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

/**
 * Request DTO for creating bookings
 */
@Data
public class BookingRequestDTO {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    private String spotId; // Required for rental products
    
    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    private LocalDate checkOutDate;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 10, message = "Maximum quantity is 10")
    private int quantity = 1;
    
    @AssertTrue(message = "Check-out date must be after check-in date")
    public boolean isValidDateRange() {
        if (checkInDate == null || checkOutDate == null) {
            return true; // Let @NotNull handle null validation
        }
        return checkOutDate.isAfter(checkInDate);
    }
}