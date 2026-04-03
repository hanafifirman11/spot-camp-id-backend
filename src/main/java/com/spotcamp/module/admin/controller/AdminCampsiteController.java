package com.spotcamp.module.admin.controller;

import com.spotcamp.module.admin.dto.AdminCampsiteListResponseDTO;
import com.spotcamp.module.admin.service.AdminCampsiteService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/campsites")
@RequiredArgsConstructor
@Tag(name = "Admin Campsites", description = "Superadmin read-only campsite management")
@SecurityRequirement(name = "BearerAuth")
public class AdminCampsiteController {

    private final AdminCampsiteService adminCampsiteService;

    @Operation(summary = "List campsites")
    @ApiResponse(responseCode = "200", description = "Campsite list")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminCampsiteListResponseDTO> listCampsites(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) CampsiteStatus status,
        @RequestParam(required = false) Long businessId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Admin list campsites page={} size={} query={} businessId={}", page, size, query, businessId);
        return ResponseEntity.ok(adminCampsiteService.listCampsites(query, status, businessId, page, size));
    }
}
