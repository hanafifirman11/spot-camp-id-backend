package com.spotcamp.module.visualmap.controller;

import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.security.UserPrincipal;
import com.spotcamp.module.visualmap.dto.*;
import com.spotcamp.module.visualmap.entity.SpotDefinition;
import com.spotcamp.module.visualmap.entity.MapConfiguration;
import com.spotcamp.module.visualmap.service.MapConfigurationService;
import com.spotcamp.module.visualmap.service.SpotAvailabilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Map Controller matching frontend API paths
 * Path: /campsites/{campsiteId}/map
 */
@Slf4j
@RestController
@RequestMapping("/campsites/{campsiteId}/map")
@RequiredArgsConstructor
@Tag(name = "Maps", description = "Visual Map Configuration and Availability")
public class CampsiteMapController {

    private final MapConfigurationService mapConfigService;
    private final SpotAvailabilityService spotAvailabilityService;

    @Value("${app.upload.maps-dir}")
    private String mapsUploadDir;

    @Operation(summary = "Get active map configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Map configuration"),
        @ApiResponse(responseCode = "404", description = "Map configuration not found")
    })
    @GetMapping
    public ResponseEntity<MapConfigurationResponseDTO> getMapConfig(
            @Parameter(description = "Campsite ID")
            @PathVariable Long campsiteId,
            @RequestParam(required = false) String mapCode,
            @RequestParam(required = false) Long mapId
    ) {
        log.debug("Get map config for campsite: {}", campsiteId);

        try {
            MapConfiguration config;
            if (mapCode != null && !mapCode.trim().isEmpty()) {
                config = mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode);
            } else if (mapId != null) {
                config = mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId);
            } else {
                config = mapConfigService.getActiveConfiguration(campsiteId);
            }
            return ResponseEntity.ok(mapToResponse(config));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "List active map configurations")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Map configurations list")
    })
    @GetMapping("/configs")
    public ResponseEntity<List<MapSummaryResponseDTO>> listMapConfigs(
            @PathVariable Long campsiteId
    ) {
        List<MapConfiguration> configs = mapConfigService.getActiveConfigurations(campsiteId);
        List<MapSummaryResponseDTO> response = configs.stream()
                .map(this::mapToSummary)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get map configuration by id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Map configuration"),
        @ApiResponse(responseCode = "404", description = "Map configuration not found")
    })
    @GetMapping("/configs/{configId}")
    public ResponseEntity<MapConfigurationResponseDTO> getMapConfigById(
            @PathVariable Long campsiteId,
            @PathVariable Long configId
    ) {
        try {
            MapConfiguration config = mapConfigService.getConfigurationForCampsite(campsiteId, configId);
            return ResponseEntity.ok(mapToResponse(config));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Save map configuration")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Map configuration saved"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<MapConfigurationResponseDTO> saveMapConfig(
            @PathVariable Long campsiteId,
            @Valid @RequestBody MapConfigurationRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Save map config for campsite: {} by user: {}", campsiteId, principal.getId());

        MapConfiguration saved = mapConfigService.saveConfiguration(
                campsiteId,
                request.getMapCode(),
                request.getMapName(),
                request.getBackgroundImageUrl(),
                request.getImageWidth(),
                request.getImageHeight(),
                request.getSpots()
        );

        return ResponseEntity.ok(mapToResponse(saved));
    }

    @Operation(summary = "Get map with spot availability")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Map availability data"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Map configuration not found")
    })
    @GetMapping("/availability")
    public ResponseEntity<MapAvailabilityResponseDTO> getMapAvailability(
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
            MapConfiguration config;
            if (mapCode != null && !mapCode.trim().isEmpty()) {
                config = mapConfigService.getActiveConfigurationByCode(campsiteId, mapCode);
            } else if (mapId != null) {
                config = mapConfigService.getActiveConfigurationForCampsite(campsiteId, mapId);
            } else {
                config = mapConfigService.getActiveConfiguration(campsiteId);
            }

            List<SpotAvailabilityDTO> availabilityList = spotAvailabilityService.getSpotAvailability(
                    campsiteId, checkIn, checkOut, config.getConfigData().getSpots()
            );

            // Convert to map format
            Map<String, String> availability = new HashMap<>();
            for (SpotAvailabilityDTO spot : availabilityList) {
                availability.put(spot.getSpotId(), spot.getStatus().name());
            }

            MapAvailabilityResponseDTO response = MapAvailabilityResponseDTO.builder()
                    .campsiteId(config.getCampsiteId())
                    .imageWidth(config.getImageWidth())
                    .imageHeight(config.getImageHeight())
                    .backgroundImageUrl(config.getBackgroundImageUrl())
                    .spots(config.getConfigData().getSpots())
                    .availability(availability)
                    .build();

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Upload map background image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Image uploaded"),
        @ApiResponse(responseCode = "400", description = "Invalid file"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping(value = "/upload-background", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<UploadBackgroundResponseDTO> uploadMapBackground(
            @PathVariable Long campsiteId,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Upload map background for campsite: {} by user: {}", campsiteId, principal.getId());

        // Validate file type
        String contentType = image.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest().build();
        }

        // Save file
        String fileName = UUID.randomUUID() + "_" + image.getOriginalFilename();
        String fileUrl = "/api/v1/uploads/maps/" + campsiteId + "/" + fileName;

        try {
            Path uploadPath = Paths.get(mapsUploadDir, String.valueOf(campsiteId));
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
        } catch (IOException e) {
            log.error("Failed to upload image", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity.ok(UploadBackgroundResponseDTO.of(fileUrl));
    }

    private MapConfigurationResponseDTO mapToResponse(MapConfiguration config) {
        return MapConfigurationResponseDTO.builder()
                .id(config.getId())
                .campsiteId(config.getCampsiteId())
                .mapCode(config.getMapCode())
                .mapName(config.getMapName())
                .imageWidth(config.getImageWidth())
                .imageHeight(config.getImageHeight())
                .backgroundImageUrl(config.getBackgroundImageUrl())
                .spots(config.getConfigData().getSpots())
                .version(config.getVersionNumber())
                .createdAt(config.getCreatedAt())
                .updatedAt(config.getUpdatedAt())
                .build();
    }

    private MapSummaryResponseDTO mapToSummary(MapConfiguration config) {
        return MapSummaryResponseDTO.builder()
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
                .build();
    }

    private List<Long> extractProductIds(MapConfiguration config) {
        if (config.getConfigData() == null || config.getConfigData().getSpots() == null) {
            return List.of();
        }
        return config.getConfigData().getSpots().stream()
                .map(SpotDefinition::getProductId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }
}
