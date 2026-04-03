package com.spotcamp.visualmap.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Map configuration data stored as JSON
 * Contains all spot definitions and their coordinates
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MapConfigData {

    @JsonProperty("spots")
    private List<SpotDefinition> spots;

    /**
     * Find a spot by ID
     */
    public SpotDefinition findSpotById(String spotId) {
        return spots != null ? 
               spots.stream()
                   .filter(spot -> spot.getId().equals(spotId))
                   .findFirst()
                   .orElse(null) : null;
    }

    /**
     * Check if a spot exists
     */
    public boolean hasSpot(String spotId) {
        return findSpotById(spotId) != null;
    }

    /**
     * Get total number of spots
     */
    @JsonIgnore
    public int getSpotCount() {
        return spots != null ? spots.size() : 0;
    }
}
