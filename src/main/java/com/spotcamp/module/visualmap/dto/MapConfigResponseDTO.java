package com.spotcamp.module.visualmap.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for map configuration operations
 * Matches the MapConfigResponseDTO schema in OpenAPI specification
 */
@Data
@Builder
public class MapConfigResponseDTO {

    private Long id;
    private Long campsiteId;
    private String mapCode;
    private String mapName;
    private Integer version;
    private Integer spotCount;
    private LocalDateTime createdAt;
}
