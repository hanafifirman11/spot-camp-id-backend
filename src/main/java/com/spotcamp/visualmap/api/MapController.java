package com.spotcamp.visualmap.api;

import com.spotcamp.visualmap.api.dto.MapConfigRequest;
import com.spotcamp.visualmap.api.dto.MapConfigResponse;
import com.spotcamp.visualmap.api.dto.MapViewResponse;
import com.spotcamp.visualmap.api.dto.SpotAvailabilityDto;
import com.spotcamp.visualmap.domain.MapConfiguration;
import com.spotcamp.visualmap.service.MapConfigurationService;
import com.spotcamp.visualmap.service.SpotAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * REST Controller for visual map operations
 * Implements the visual map endpoints defined in the OpenAPI specification
 */
@Slf4j
@RestController
@RequestMapping("/maps")
@RequiredArgsConstructor
@Tag(name = "Visual Map", description = "Map configuration and spot management")
public class MapController {

    private final MapConfigurationService mapConfigService;
    private final SpotAvailabilityService spotAvailabilityService;

    @Operation(
        summary = "Get map configuration",
        description = "Retrieve the active map configuration for a campsite"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map configuration retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MapConfiguration.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Map configuration not found"
        )
    })
    @GetMapping("/{campsiteId}/config")
    public ResponseEntity<MapConfiguration> getMapConfig(
            @Parameter(description = "Unique identifier of the campsite")
            @PathVariable Long campsiteId,
            @RequestParam(required = false) String mapCode,
            @RequestParam(required = false) Long mapId
    ) {
        log.debug("Getting map configuration for campsite: {}", campsiteId);

        MapConfiguration config;
        if (mapCode != null && !mapCode.trim().isEmpty()) {
            config = mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode);
        } else if (mapId != null) {
            config = mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId);
        } else {
            config = mapConfigService.getActiveConfiguration(campsiteId);
        }
        return ResponseEntity.ok(config);
    }

    @Operation(
        summary = "Save map configuration",
        description = "Create or update map configuration (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map configuration saved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MapConfigResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Access denied"
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Payload too large (>5MB)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2026-01-07T10:30:00Z",
                        "status": 413,
                        "error": "Payload Too Large",
                        "message": "Request entity too large",
                        "path": "/api/v1/maps/1/config"
                    }
                """)
            )
        )
    })
    @PostMapping("/{campsiteId}/config")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MapConfigResponse> saveMapConfig(
            @Parameter(description = "Unique identifier of the campsite")
            @PathVariable Long campsiteId,
            @Valid @RequestBody MapConfigRequest request
    ) {
        log.info("Saving map configuration for campsite: {}", campsiteId);
        
        MapConfiguration saved = mapConfigService.saveConfiguration(
                campsiteId,
                request.getMapCode(),
                request.getMapName(),
                request.getBackgroundImageUrl(),
                request.getImageWidth(),
                request.getImageHeight(),
                request.getSpots()
        );
        
        MapConfigResponse response = MapConfigResponse.builder()
                .id(saved.getId())
                .campsiteId(saved.getCampsiteId())
                .mapCode(saved.getMapCode())
                .mapName(saved.getMapName())
                .version(saved.getVersionNumber())
                .spotCount(saved.getSpotCount())
                .createdAt(saved.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get map view with availability",
        description = "Retrieve map configuration with real-time spot availability. Uses CQRS read model for optimized performance (<100ms)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map view retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MapViewResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid date range",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2026-01-07T10:30:00Z",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Check-in date must be before check-out date",
                        "path": "/api/v1/maps/1/view"
                    }
                """)
            )
        )
    })
    @GetMapping("/{campsiteId}/view")
    @Cacheable(value = "mapView", key = "#campsiteId + ':' + #checkIn + ':' + #checkOut")
    public ResponseEntity<MapViewResponse> getMapView(
            @Parameter(description = "Unique identifier of the campsite")
            @PathVariable Long campsiteId,
            @RequestParam(required = false) String mapCode,
            @RequestParam(required = false) Long mapId,
            
            @Parameter(description = "Check-in date (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            
            @Parameter(description = "Check-out date (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        log.debug("Getting map view for campsite: {} from {} to {}", campsiteId, checkIn, checkOut);
        
        // Validate date range
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            return ResponseEntity.badRequest().build();
        }
        
        // Get map configuration
        MapConfiguration config;
        if (mapCode != null && !mapCode.trim().isEmpty()) {
            config = mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode);
        } else if (mapId != null) {
            config = mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId);
        } else {
            config = mapConfigService.getActiveConfiguration(campsiteId);
        }
        
        // Get spot availability
        List<SpotAvailabilityDto> availability = spotAvailabilityService.getSpotAvailability(
                campsiteId, checkIn, checkOut, config.getConfigData().getSpots()
        );
        
        MapViewResponse response = MapViewResponse.builder()
                .campsiteId(config.getCampsiteId())
                .backgroundImageUrl(config.getBackgroundImageUrl())
                .imageWidth(config.getImageWidth())
                .imageHeight(config.getImageHeight())
                .spots(config.getConfigData().getSpots())
                .availability(availability)
                .build();
        
        // Set cache control headers for better performance
        CacheControl cacheControl = CacheControl.maxAge(5, TimeUnit.MINUTES)
                .cachePublic()
                .mustRevalidate();
        
        return ResponseEntity.ok()
                .cacheControl(cacheControl)
                .body(response);
    }

    @Operation(
        summary = "Activate map configuration",
        description = "Activate a draft map configuration (Merchant/Admin only)"
    )
    @PostMapping("/{campsiteId}/config/{configId}/activate")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<MapConfigResponse> activateConfig(
            @PathVariable Long campsiteId,
            @PathVariable Long configId
    ) {
        log.info("Activating map configuration: {} for campsite: {}", configId, campsiteId);
        
        MapConfiguration activated = mapConfigService.activateConfiguration(campsiteId, configId);
        
        MapConfigResponse response = MapConfigResponse.builder()
                .id(activated.getId())
                .campsiteId(activated.getCampsiteId())
                .mapCode(activated.getMapCode())
                .mapName(activated.getMapName())
                .version(activated.getVersionNumber())
                .spotCount(activated.getSpotCount())
                .createdAt(activated.getCreatedAt())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete map configuration",
        description = "Delete a draft map configuration (Merchant/Admin only)"
    )
    @DeleteMapping("/{campsiteId}/config/{configId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteConfig(
            @PathVariable Long campsiteId,
            @PathVariable Long configId
    ) {
        log.info("Deleting map configuration: {} for campsite: {}", configId, campsiteId);
        
        mapConfigService.deleteConfiguration(campsiteId, configId);
        return ResponseEntity.noContent().build();
    }
}
