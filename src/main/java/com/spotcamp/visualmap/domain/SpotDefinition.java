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
 * Definition of a camping spot on the visual map
 * Contains coordinates and metadata for each spot
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SpotDefinition {

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("productId")
    private Long productId;

    @JsonProperty("coordinates")
    private List<Coordinate> coordinates;

    @JsonProperty("description")
    private String description;

    /**
     * Validates if the spot definition is complete
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
               name != null && !name.trim().isEmpty() &&
               productId != null &&
               coordinates != null &&
               coordinates.size() >= 3 && // Minimum for a polygon
               coordinates.stream().allMatch(Coordinate::isValid);
    }

    /**
     * Gets the center point of the spot polygon
     */
    @JsonIgnore
    public Coordinate getCenterPoint() {
        if (coordinates == null || coordinates.isEmpty()) {
            return null;
        }
        
        double avgX = coordinates.stream().mapToDouble(Coordinate::getX).average().orElse(0.0);
        double avgY = coordinates.stream().mapToDouble(Coordinate::getY).average().orElse(0.0);
        
        return Coordinate.builder().x(avgX).y(avgY).build();
    }

    /**
     * Calculates the approximate area of the spot polygon
     */
    @JsonIgnore
    public double getArea() {
        if (coordinates == null || coordinates.size() < 3) {
            return 0.0;
        }
        
        double area = 0.0;
        int n = coordinates.size();
        
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            area += coordinates.get(i).getX() * coordinates.get(j).getY();
            area -= coordinates.get(j).getX() * coordinates.get(i).getY();
        }
        
        return Math.abs(area) / 2.0;
    }
}
