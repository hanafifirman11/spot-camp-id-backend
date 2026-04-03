package com.spotcamp.module.admin.controller;

import com.spotcamp.module.admin.dto.AdminUserListResponseDTO;
import com.spotcamp.module.admin.service.AdminUserService;
import com.spotcamp.module.authuser.entity.UserRole;
import com.spotcamp.module.authuser.entity.UserStatus;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin Users", description = "Superadmin read-only user management")
@SecurityRequirement(name = "BearerAuth")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @Operation(summary = "List users")
    @ApiResponse(responseCode = "200", description = "User list")
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<AdminUserListResponseDTO> listUsers(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) UserStatus status,
        @RequestParam(required = false) UserRole role,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        log.debug("Admin list users page={} size={} query={} role={}", page, size, query, role);
        return ResponseEntity.ok(adminUserService.listUsers(query, status, role, page, size));
    }
}
