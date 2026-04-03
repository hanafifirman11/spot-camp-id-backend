package com.spotcamp.module.authuser.controller;

import com.spotcamp.module.authuser.dto.CreateMerchantUserRequestDTO;
import com.spotcamp.module.authuser.dto.UpdateMerchantUserRoleRequestDTO;
import com.spotcamp.module.authuser.entity.User;
import com.spotcamp.module.authuser.service.MerchantUserService;
import com.spotcamp.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchant/users")
@RequiredArgsConstructor
@Tag(name = "Merchant User Management", description = "Manage users within a merchant account")
@SecurityRequirement(name = "BearerAuth")
public class MerchantUserController {

    private final MerchantUserService merchantUserService;

    @Operation(summary = "List merchant users")
    @GetMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<List<User>> listUsers(@AuthenticationPrincipal UserPrincipal currentUser) {
        String businessCode = currentUser.getUser().getBusinessCode();
        if (businessCode == null) {
            throw new IllegalStateException("User does not have a business code");
        }
        return ResponseEntity.ok(merchantUserService.getMerchantUsers(businessCode));
    }

    @Operation(summary = "Create merchant user")
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<User> createUser(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody CreateMerchantUserRequestDTO request) {
        String businessCode = currentUser.getUser().getBusinessCode();
        return ResponseEntity.ok(merchantUserService.createMerchantUser(
                businessCode,
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getRole()
        ));
    }

    @Operation(summary = "Update user role")
    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<User> updateUserRole(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long userId,
            @RequestBody UpdateMerchantUserRoleRequestDTO request) {
        String businessCode = currentUser.getUser().getBusinessCode();
        return ResponseEntity.ok(merchantUserService.updateUserRole(userId, businessCode, request.getRole()));
    }

    @Operation(summary = "Toggle user status")
    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    public ResponseEntity<User> toggleUserStatus(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable Long userId) {
        String businessCode = currentUser.getUser().getBusinessCode();
        return ResponseEntity.ok(merchantUserService.toggleUserStatus(userId, businessCode));
    }
}
