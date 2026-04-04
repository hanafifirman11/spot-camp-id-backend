package com.spotcamp.module.inventory.controller;

import com.spotcamp.security.UserPrincipal;
import com.spotcamp.module.inventory.dto.BundleDefinitionDTO;
import com.spotcamp.module.inventory.dto.BundleComponentRequest;
import com.spotcamp.module.inventory.dto.BundleRequestDTO;
import com.spotcamp.module.inventory.entity.Bundle;
import com.spotcamp.module.inventory.service.BundleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Bundle Controller for campsite-scoped operations
 * Matches frontend API paths: /campsites/{campsiteId}/bundles
 */
@Slf4j
@RestController
@RequestMapping("/campsites/{campsiteId}/bundles")
@RequiredArgsConstructor
@Tag(name = "Bundles", description = "Bundle Management")
public class CampsiteBundleController {

    private final BundleService bundleService;

    @Operation(summary = "List bundles for a campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bundle list")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Map<String, Object>> listCampsiteBundles(
            @PathVariable Long campsiteId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("List bundles for campsite: {}", campsiteId);

        Page<Bundle> bundles = bundleService.getBundles(campsiteId, page, size);

        List<BundleDefinitionDTO> content = bundles.getContent().stream()
                .map(this::mapToBundleDto)
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("content", content);
        response.put("page", Map.of(
                "number", page,
                "size", size,
                "totalElements", bundles.getTotalElements(),
                "totalPages", bundles.getTotalPages()
        ));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create bundle for a campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bundle created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<BundleDefinitionDTO> createCampsiteBundle(
            @PathVariable Long campsiteId,
            @Valid @RequestBody BundleRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Create bundle for campsite: {} by user: {}", campsiteId, principal.getId());

        List<BundleComponentRequest> components = request.getComponents().stream()
                .map(comp -> {
                    BundleComponentRequest componentReq = new BundleComponentRequest();
                    componentReq.setProductId(comp.getProductId());
                    componentReq.setQuantity(comp.getQuantity());
                    return componentReq;
                })
                .collect(Collectors.toList());

        Bundle bundle = bundleService.createBundle(
                campsiteId,
                request.getName(),
                request.getDescription(),
                request.getBundlePrice(),
                components
        );

        return new ResponseEntity<>(mapToBundleDto(bundle), HttpStatus.CREATED);
    }

    private BundleDefinitionDTO mapToBundleDto(Bundle bundle) {
        List<BundleDefinitionDTO.BundleComponentDTO> components = bundle.getComponents().stream()
                .map(comp -> BundleDefinitionDTO.BundleComponentDTO.builder()
                        .productId(comp.getProduct().getId())
                        .productName(comp.getProduct().getName())
                        .quantity(comp.getQuantity())
                        .build())
                .collect(Collectors.toList());

        return BundleDefinitionDTO.builder()
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
