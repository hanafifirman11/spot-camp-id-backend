package com.spotcamp.module.visualmap.dto;

import com.spotcamp.module.visualmap.entity.SpotDefinition;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for map view with availability
 * Matches the MapViewResponseDTO schema in OpenAPI specification
 */
@Data
@Builder
public class MapViewResponseDTO {

    private Long campsiteId;
    private String backgroundImageUrl;
    private Integer imageWidth;
    private Integer imageHeight;
    private List<SpotDefinition> spots;
    private List<SpotAvailabilityDTO> availability;
}