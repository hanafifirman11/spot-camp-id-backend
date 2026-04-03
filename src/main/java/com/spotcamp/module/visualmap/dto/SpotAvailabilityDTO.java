package com.spotcamp.module.visualmap.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO for spot availability information
 * Matches the SpotAvailability schema in OpenAPI specification
 */
@Data
@Builder
public class SpotAvailabilityDTO {

    private String spotId;
    private SpotStatus status;
    private LocalDateTime lockedUntil;

    /**
     * Spot availability status
     */
    public enum SpotStatus {
        /**
         * Can be booked
         */
        AVAILABLE,
        
        /**
         * Already confirmed/paid
         */
        BOOKED,
        
        /**
         * In another user's cart
         */
        LOCKED
    }
}