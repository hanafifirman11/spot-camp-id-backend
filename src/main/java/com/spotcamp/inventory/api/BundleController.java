package com.spotcamp.inventory.api;

import com.spotcamp.inventory.api.dto.BundleDefinitionDto;
import com.spotcamp.inventory.api.dto.BundleRequest;
import com.spotcamp.inventory.domain.Bundle;
import com.spotcamp.inventory.service.BundleService;
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
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for bundle management
 * Implements the bundle endpoints defined in the OpenAPI specification
 */
@Slf4j
@RestController
@RequestMapping("/bundles")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Product and bundle management")
public class BundleController {

    private final BundleService bundleService;

    @Operation(
        summary = "List bundles",
        description = "Get paginated list of bundles"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bundles retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<BundleDefinitionDto>> listBundles(
            @Parameter(description = "Filter by campsite ID")
            @RequestParam(required = false) Long campsiteId,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size
    ) {
        log.debug("Getting bundles for campsite: {}", campsiteId);
        
        Page<Bundle> bundles = bundleService.getBundles(campsiteId, page, size);
        Page<BundleDefinitionDto> response = bundles.map(this::mapToBundleDefinitionDto);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Create bundle",
        description = "Create a new product bundle (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Bundle created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BundleDefinitionDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BundleDefinitionDto> createBundle(
            @Valid @RequestBody BundleRequest request
    ) {
        log.info("Creating new bundle: {}", request.getName());
        
        List<BundleService.BundleComponentRequest> components = request.getComponents().stream()
                .map(comp -> {
                    BundleService.BundleComponentRequest componentReq = new BundleService.BundleComponentRequest();
                    componentReq.setProductId(comp.getProductId());
                    componentReq.setQuantity(comp.getQuantity());
                    return componentReq;
                })
                .collect(Collectors.toList());
        
        Bundle bundle = bundleService.createBundle(
                request.getCampsiteId(),
                request.getName(),
                request.getDescription(),
                request.getBundlePrice(),
                components
        );
        
        BundleDefinitionDto response = mapToBundleDefinitionDto(bundle);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get bundle details",
        description = "Retrieve detailed information about a specific bundle"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bundle details retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BundleDefinitionDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bundle not found"
        )
    })
    @GetMapping("/{bundleId}")
    public ResponseEntity<BundleDefinitionDto> getBundle(
            @Parameter(description = "Unique identifier of the bundle")
            @PathVariable Long bundleId
    ) {
        log.debug("Getting bundle details for ID: {}", bundleId);
        
        Bundle bundle = bundleService.getBundle(bundleId);
        BundleDefinitionDto response = mapToBundleDefinitionDto(bundle);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update bundle",
        description = "Update an existing bundle (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Bundle updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BundleDefinitionDto.class)
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
            responseCode = "404",
            description = "Bundle not found"
        )
    })
    @PutMapping("/{bundleId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<BundleDefinitionDto> updateBundle(
            @Parameter(description = "Unique identifier of the bundle")
            @PathVariable Long bundleId,
            @Valid @RequestBody BundleRequest request
    ) {
        log.info("Updating bundle: {}", bundleId);
        
        List<BundleService.BundleComponentRequest> components = request.getComponents().stream()
                .map(comp -> {
                    BundleService.BundleComponentRequest componentReq = new BundleService.BundleComponentRequest();
                    componentReq.setProductId(comp.getProductId());
                    componentReq.setQuantity(comp.getQuantity());
                    return componentReq;
                })
                .collect(Collectors.toList());
        
        Bundle bundle = bundleService.updateBundle(
                bundleId,
                request.getName(),
                request.getDescription(),
                request.getBundlePrice(),
                components
        );
        
        BundleDefinitionDto response = mapToBundleDefinitionDto(bundle);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete bundle",
        description = "Archive a bundle (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Bundle deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Bundle not found"
        )
    })
    @DeleteMapping("/{bundleId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteBundle(
            @Parameter(description = "Unique identifier of the bundle")
            @PathVariable Long bundleId
    ) {
        log.info("Deleting bundle: {}", bundleId);
        
        bundleService.deleteBundle(bundleId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
        summary = "Check bundle availability",
        description = "Check if all bundle components are available for given dates"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Availability checked",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "available": true,
                        "unavailableComponents": []
                    }
                """)
            )
        )
    })
    @GetMapping("/{bundleId}/availability")
    public ResponseEntity<Map<String, Object>> checkBundleAvailability(
            @Parameter(description = "Unique identifier of the bundle")
            @PathVariable Long bundleId,
            
            @Parameter(description = "Check-in date (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            
            @Parameter(description = "Check-out date (ISO 8601)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut
    ) {
        log.debug("Checking availability for bundle: {} from {} to {}", bundleId, checkIn, checkOut);
        
        BundleService.BundleAvailabilityResult result = bundleService.checkBundleAvailability(
                bundleId, checkIn, checkOut);
        
        Map<String, Object> response = Map.of(
                "available", result.isAvailable(),
                "unavailableComponents", result.getUnavailableComponents() != null ? 
                    result.getUnavailableComponents() : List.of()
        );
        
        return ResponseEntity.ok(response);
    }

    /**
     * Map Bundle entity to BundleDefinitionDto
     */
    private BundleDefinitionDto mapToBundleDefinitionDto(Bundle bundle) {
        List<BundleDefinitionDto.BundleComponentDto> components = bundle.getComponents().stream()
                .map(comp -> BundleDefinitionDto.BundleComponentDto.builder()
                        .productId(comp.getProduct().getId())
                        .productName(comp.getProduct().getName())
                        .quantity(comp.getQuantity())
                        .build())
                .collect(Collectors.toList());
        
        return BundleDefinitionDto.builder()
                .id(bundle.getId())
                .campsiteId(bundle.getCampsiteId())
                .name(bundle.getName())
                .description(bundle.getDescription())
                .bundlePrice(bundle.getBundlePrice())
                .components(components)
                .createdAt(bundle.getCreatedAt())
                .build();
    }
}