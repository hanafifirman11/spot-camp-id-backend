package com.spotcamp.module.admin.controller;

import com.spotcamp.module.admin.dto.AdminBusinessCampsiteListResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessDetailResponseDTO;
import com.spotcamp.module.admin.dto.AdminBusinessListResponseDTO;
import com.spotcamp.module.admin.service.AdminBusinessService;
import com.spotcamp.module.authuser.entity.UserStatus;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/businesses")
@RequiredArgsConstructor
@Tag(name = "Admin Businesses", description = "Superadmin read-only business management")
@SecurityRequirement(name = "BearerAuth")
public class AdminBusinessController {

    private final AdminBusinessService adminBusinessService;

    @Operation(summary = "List businesses (merchants)")
    @ApiResponse(responseCode = "200", description = "Business list")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminBusinessListResponseDTO> listBusinesses(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Admin list businesses page={} size={} query={}", page, size, query);
        AdminBusinessListResponseDTO response = adminBusinessService.listBusinesses(query, status, page, size);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get business detail")
    @ApiResponse(responseCode = "200", description = "Business detail")
    @GetMapping("/{businessId}")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminBusinessDetailResponseDTO> getBusiness(@PathVariable Long businessId) {
        log.debug("Admin get business {}", businessId);
        return ResponseEntity.ok(adminBusinessService.getBusiness(businessId));
    }

    @Operation(summary = "List campsites for a business")
    @ApiResponse(responseCode = "200", description = "Business campsites list")
    @GetMapping("/{businessId}/campsites")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminBusinessCampsiteListResponseDTO> listBusinessCampsites(
        @PathVariable Long businessId,
        @RequestParam(required = false) CampsiteStatus status,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Admin list campsites for business {} page={}", businessId, page);
        return ResponseEntity.ok(adminBusinessService.listBusinessCampsites(businessId, status, page, size));
    }
}
