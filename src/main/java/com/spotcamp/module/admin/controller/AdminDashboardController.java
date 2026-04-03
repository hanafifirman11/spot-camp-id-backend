package com.spotcamp.module.admin.controller;

import com.spotcamp.module.admin.dto.AdminDashboardSummaryResponseDTO;
import com.spotcamp.module.admin.service.AdminDashboardService;
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
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/admin/dashboard")
@RequiredArgsConstructor
@Tag(name = "Admin Dashboard", description = "Superadmin dashboard summary")
@SecurityRequirement(name = "BearerAuth")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @Operation(summary = "Get admin dashboard summary")
    @ApiResponse(responseCode = "200", description = "Summary data")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminDashboardSummaryResponseDTO> getSummary() {
        log.debug("Admin dashboard summary");
        return ResponseEntity.ok(adminDashboardService.getSummary());
    }
}
