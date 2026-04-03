package com.spotcamp.campsite.api;

import com.spotcamp.campsite.api.dto.*;
import com.spotcamp.campsite.domain.CampsiteStatus;
import com.spotcamp.campsite.service.CampsiteService;
import com.spotcamp.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * Campsite Management Controller for Merchants
 */
@Slf4j
@RestController
@RequestMapping("/campsites")
@RequiredArgsConstructor
@Tag(name = "Campsites", description = "Campsite Management (Merchant)")
@SecurityRequirement(name = "BearerAuth")
public class CampsiteController {

    private final CampsiteService campsiteService;

    @Operation(summary = "List merchant's campsites")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "List of campsites"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteListResponse> listCampsites(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CampsiteStatus status
    ) {
        log.debug("List campsites for merchant: {}", principal.getId());
        CampsiteListResponse response = campsiteService.listMerchantCampsites(principal.getId(), status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get campsite details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campsite details"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @GetMapping("/{campsiteId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteDetailResponse> getCampsite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId
    ) {
        log.debug("Get campsite: {} for merchant: {}", campsiteId, principal.getId());
        CampsiteDetailResponse response = campsiteService.getCampsite(campsiteId, principal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create new campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Campsite created"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteResponse> createCampsite(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CampsiteRequest request
    ) {
        log.info("Create campsite for merchant: {}", principal.getId());
        CampsiteResponse response = campsiteService.createCampsite(principal.getId(), request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campsite updated"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @PutMapping("/{campsiteId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteResponse> updateCampsite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @Valid @RequestBody CampsiteRequest request
    ) {
        log.info("Update campsite: {} for merchant: {}", campsiteId, principal.getId());
        CampsiteResponse response = campsiteService.updateCampsite(campsiteId, principal.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete campsite (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Campsite deleted"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @DeleteMapping("/{campsiteId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Void> deleteCampsite(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId
    ) {
        log.info("Delete campsite: {} for merchant: {}", campsiteId, principal.getId());
        campsiteService.deleteCampsite(campsiteId, principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update campsite status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Campsite status updated"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @PatchMapping("/{campsiteId}/status")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteResponse> updateCampsiteStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @Valid @RequestBody UpdateCampsiteStatusRequest request
    ) {
        log.info("Update campsite status: {} for merchant: {}", campsiteId, principal.getId());
        CampsiteResponse response = campsiteService.updateStatus(campsiteId, principal.getId(), request.getStatus());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get merchant campsite summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Summary for merchant campsites")
    })
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteSummaryResponse> getSummary(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        CampsiteSummaryResponse response = campsiteService.getMerchantSummary(principal.getId());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Add campsite image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Image uploaded"),
        @ApiResponse(responseCode = "400", description = "Invalid file")
    })
    @PostMapping(value = "/{campsiteId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteImageResponse> addCampsiteImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "isPrimary", required = false) Boolean isPrimary
    ) {
        log.info("Add image to campsite: {} for merchant: {}", campsiteId, principal.getId());
        CampsiteImageResponse response = campsiteService.addImage(campsiteId, principal.getId(), image, caption, isPrimary);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Set primary campsite image")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Primary image updated"),
        @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @PutMapping("/{campsiteId}/images/{imageId}/primary")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<CampsiteImageResponse> setPrimaryImage(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @PathVariable Long imageId
    ) {
        log.info("Set primary image {} for campsite: {}", imageId, campsiteId);
        CampsiteImageResponse response = campsiteService.setPrimaryImage(campsiteId, principal.getId(), imageId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update campsite amenities")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Amenities updated"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @PutMapping("/{campsiteId}/amenities")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AmenityResponse> updateCampsiteAmenities(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @Valid @RequestBody UpdateAmenitiesRequest request
    ) {
        log.info("Update amenities for campsite: {}", campsiteId);
        AmenityResponse response = campsiteService.updateAmenities(campsiteId, principal.getId(), request.getAmenityIds());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update campsite rules")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Rules updated"),
        @ApiResponse(responseCode = "404", description = "Campsite not found")
    })
    @PutMapping("/{campsiteId}/rules")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> updateCampsiteRules(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long campsiteId,
            @Valid @RequestBody UpdateRulesRequest request
    ) {
        log.info("Update rules for campsite: {}", campsiteId);
        var rules = campsiteService.updateRules(campsiteId, principal.getId(), request.getRules());
        return ResponseEntity.ok(Map.of("rules", rules));
    }

    @Operation(summary = "List all available amenities")
    @GetMapping("/amenities")
    public ResponseEntity<AmenityResponse> listAmenities() {
        AmenityResponse response = campsiteService.listAmenities();
        return ResponseEntity.ok(response);
    }
}
