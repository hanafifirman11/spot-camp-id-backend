package com.spotcamp.visualmap.api.dto;

import com.spotcamp.visualmap.domain.SpotDefinition;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for map view with availability
 * Matches the MapViewResponse schema in OpenAPI specification
 */
@Data
@Builder
public class MapViewResponse {

    private Long campsiteId;
    private String backgroundImageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private List<SpotDefinition> spots;
    private List<SpotAvailabilityDto> availability;
}