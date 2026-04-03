package com.spotcamp.publicmarket.api;

import com.spotcamp.publicmarket.api.dto.CampsitePublicDetail;
import com.spotcamp.publicmarket.api.dto.CampsitePublicResponse;
import com.spotcamp.publicmarket.api.dto.MapConfigPublicResponse;
import com.spotcamp.publicmarket.service.PublicCampsiteService;
import com.spotcamp.visualmap.api.dto.MapSummaryResponse;
import com.spotcamp.visualmap.api.dto.SpotAvailabilityDto;
import com.spotcamp.visualmap.service.MapConfigurationService;
import com.spotcamp.visualmap.service.SpotAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Public API Controller for marketplace discovery
 * No authentication required
 */
@Slf4j
@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Public", description = "Public marketplace endpoints - no authentication required")
public class PublicController {

    private final PublicCampsiteService publicCampsiteService;
    private final SpotAvailabilityService spotAvailabilityService;
    private final MapConfigurationService mapConfigService;

    @Operation(
        summary = "Search and list campsites",
        description = "Primary endpoint for the discovery page. Supports full-text search, pagination, and date range filtering."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "List of campsites matching criteria",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampsitePublicResponse.class)
            )
        )
    })
    @GetMapping("/campsites")
    public ResponseEntity<List<CampsitePublicResponse>> searchCampsites(
            @Parameter(description = "Full-text search query (name, description, location)")
            @RequestParam(value = "q", required = false) String query,

            @Parameter(description = "Check-in date (ISO 8601)")
            @RequestParam(value = "checkIn", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String checkIn,

            @Parameter(description = "Check-out date (ISO 8601)")
            @RequestParam(value = "checkOut", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) String checkOut,

            @Parameter(description = "Page number for pagination")
            @RequestParam(value = "_page", defaultValue = "1") int page,

            @Parameter(description = "Items per page")
            @RequestParam(value = "_limit", defaultValue = "6") int limit
    ) {
        log.debug("Search campsites: query={}, page={}, limit={}", query, page, limit);

        Page<CampsitePublicResponse> result = publicCampsiteService.searchCampsites(query, page, limit);

        return ResponseEntity.ok(result.getContent());
    }

    @Operation(
        summary = "Get public campsite details",
        description = "Get detailed information about a campsite for public view"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Campsite detailed information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CampsitePublicDetail.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @GetMapping("/campsites/{campsiteId}")
    public ResponseEntity<CampsitePublicDetail> getCampsiteDetail(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId
    ) {
        log.debug("Get campsite detail: {}", campsiteId);

        CampsitePublicDetail detail = publicCampsiteService.getCampsiteDetail(campsiteId);
        return ResponseEntity.ok(detail);
    }

    @Operation(
        summary = "Get map configuration for visual selection",
        description = "Retrieve map image and spot definitions for a campsite"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map image and spot definitions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MapConfigPublicResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Map configuration not found")
    })
    @GetMapping("/maps/{campsiteId}/config")
    public ResponseEntity<MapConfigPublicResponse> getMapConfig(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId,
            @RequestParam(required = false) String mapCode,
            @RequestParam(required = false) Long mapId
    ) {
        log.debug("Get map config for campsite: {}", campsiteId);

        MapConfigPublicResponse config = publicCampsiteService.getMapConfig(campsiteId, mapCode, mapId);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @Operation(
        summary = "Get map configuration by id",
        description = "Retrieve map image and spot definitions for a specific map layout"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map image and spot definitions",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MapConfigPublicResponse.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Map configuration not found")
    })
    @GetMapping("/maps/{campsiteId}/configs/{mapCode}")
    public ResponseEntity<MapConfigPublicResponse> getMapConfigById(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId,
            @PathVariable String mapCode
    ) {
        log.debug("Get map config {} for campsite: {}", mapCode, campsiteId);

        MapConfigPublicResponse config = publicCampsiteService.getMapConfig(campsiteId, mapCode, null);
        if (config == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(config);
    }

    @Operation(
        summary = "List active map configurations",
        description = "Retrieve available map layouts for a campsite"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Map list retrieved"
        )
    })
    @GetMapping("/maps/{campsiteId}/configs")
    public ResponseEntity<List<MapSummaryResponse>> listMapConfigs(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId
    ) {
        List<MapSummaryResponse> response = publicCampsiteService.getActiveMapConfigs(campsiteId)
                .stream()
                .map(config -> MapSummaryResponse.builder()
                        .id(config.getId())
                        .campsiteId(config.getCampsiteId())
                        .mapCode(config.getMapCode())
                        .mapName(config.getMapName())
                        .productIds(extractProductIds(config))
                        .imageWidth(config.getImageWidth())
                        .imageHeight(config.getImageHeight())
                        .backgroundImageUrl(config.getBackgroundImageUrl())
                        .status(config.getStatus())
                        .updatedAt(config.getUpdatedAt())
                        .build())
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Get real-time spot availability",
        description = "Get status of each spot for selected dates"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Status of each spot for selected dates"
        ),
        @ApiResponse(responseCode = "400", description = "Invalid date range")
    })
    @GetMapping("/maps/{campsiteId}/availability")
    public ResponseEntity<Map<String, String>> getMapAvailability(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId,
            @RequestParam(required = false) String mapCode,
            @RequestParam(required = false) Long mapId,

            @Parameter(description = "Check-in date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,

            @Parameter(description = "Check-out date")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        log.debug("Get map availability for campsite: {} from {} to {}", campsiteId, checkIn, checkOut);

        // Validate dates
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            return ResponseEntity.badRequest().build();
        }

        try {
            var config = mapCode != null && !mapCode.trim().isEmpty()
                    ? mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode)
                    : (mapId != null
                        ? mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId)
                        : mapConfigService.getActiveConfiguration(campsiteId));
            List<SpotAvailabilityDto> availability = spotAvailabilityService.getSpotAvailability(
                    campsiteId, checkIn, checkOut, config.getConfigData().getSpots()
            );

            // Convert to map format expected by frontend
            Map<String, String> result = new HashMap<>();
            for (SpotAvailabilityDto spot : availability) {
                result.put(spot.getSpotId(), spot.getStatus().name());
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.warn("Map config not found for campsite: {}", campsiteId);
            return ResponseEntity.ok(new HashMap<>());
        }
    }

    private List<Long> extractProductIds(com.spotcamp.visualmap.domain.MapConfiguration config) {
        if (config.getConfigData() == null || config.getConfigData().getSpots() == null) {
            return List.of();
        }
        return config.getConfigData().getSpots().stream()
                .map(com.spotcamp.visualmap.domain.SpotDefinition::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
