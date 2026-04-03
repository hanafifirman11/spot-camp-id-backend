package com.spotcamp.admin.api;

import com.spotcamp.admin.api.dto.LogListResponse;
import com.spotcamp.admin.service.AdminLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/logs")
@RequiredArgsConstructor
@Tag(name = "Admin Logs", description = "System logs viewer")
@SecurityRequirement(name = "BearerAuth")
public class AdminLogController {

    private final AdminLogService adminLogService;

    @Operation(summary = "Get system logs", description = "Reads logs from file in reverse order (newest first). Supports filtering.")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<LogListResponse> getLogs(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "50") int size,
            @Parameter(description = "Log level (INFO, WARN, ERROR, DEBUG)") @RequestParam(required = false) String level,
            @Parameter(description = "Search term") @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(adminLogService.getLogs(page, size, level, search));
    }
}
