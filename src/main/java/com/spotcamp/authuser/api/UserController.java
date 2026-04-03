package com.spotcamp.authuser.api;

import com.spotcamp.authuser.api.dto.ChangePasswordRequest;
import com.spotcamp.authuser.api.dto.UpdateProfileRequest;
import com.spotcamp.authuser.api.dto.UserProfileResponse;
import com.spotcamp.authuser.domain.User;
import com.spotcamp.authuser.service.UserService;
import com.spotcamp.common.security.UserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for user management endpoints
 * Implements the user management endpoints defined in the OpenAPI specification
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User profile and preferences")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
        summary = "Get current user profile",
        description = "Retrieve the profile of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User profile retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal principal) {
        User user = principal.getUser();
        
        log.debug("Getting profile for user ID: {}", user.getId());
        
        UserProfileResponse response = UserProfileResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .avatar(user.getAvatarUrl())
                .role(user.getRole())
                .businessName(user.getBusinessName())
                .businessCode(user.getBusinessCode())
                .createdAt(user.getCreatedAt())
                .emailVerified(user.isEmailVerified())
                .darkMode(user.isDarkMode())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update user profile",
        description = "Update the profile information of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Profile updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserProfileResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2026-01-07T10:30:00Z",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Validation failed",
                        "path": "/api/v1/users/me"
                    }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = principal.getUser();
        
        log.info("Updating profile for user ID: {}", currentUser.getId());
        
        User updatedUser = userService.updateProfile(
                currentUser.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getAvatar(),
                request.getDarkMode()
        );
        
        UserProfileResponse response = UserProfileResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName())
                .phone(updatedUser.getPhone())
                .avatar(updatedUser.getAvatarUrl())
                .role(updatedUser.getRole())
                .businessName(updatedUser.getBusinessName())
                .businessCode(updatedUser.getBusinessCode())
                .createdAt(updatedUser.getCreatedAt())
                .emailVerified(updatedUser.isEmailVerified())
                .darkMode(updatedUser.isDarkMode())
                .build();
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Change password",
        description = "Change the password of the currently authenticated user"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Password changed successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "message": "Password changed successfully"
                    }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid current password or weak new password",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                        "timestamp": "2026-01-07T10:30:00Z",
                        "status": 400,
                        "error": "Bad Request",
                        "message": "Current password is incorrect",
                        "path": "/api/v1/users/me/password"
                    }
                """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PutMapping("/me/password")
    public ResponseEntity<Map<String, String>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        User currentUser = principal.getUser();
        
        log.info("Password change request for user ID: {}", currentUser.getId());
        
        userService.changePassword(
                currentUser.getId(),
                request.getCurrentPassword(),
                request.getNewPassword()
        );
        
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}
