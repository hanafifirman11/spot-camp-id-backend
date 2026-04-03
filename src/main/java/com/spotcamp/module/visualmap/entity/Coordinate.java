package com.spotcamp.module.visualmap.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Coordinate point for spot polygons on the visual map
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Coordinate {

    @JsonProperty("x")
    private Double x;

    @JsonProperty("y")
    private Double y;

    /**
     * Validates if the coordinate is valid
     */
    @JsonIgnore
    public boolean isValid() {
        return x != null && x >= 0 && y != null && y >= 0;
    }

    /**
     * Calculates distance to another coordinate
     */
    public double distanceTo(Coordinate other) {
        if (other == null) {
            return Double.MAX_VALUE;
        }
        
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        
        return Math.sqrt(dx * dx + dy * dy);
    }
}
