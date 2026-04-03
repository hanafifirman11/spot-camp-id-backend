package com.spotcamp.authuser.api.dto;

import com.spotcamp.authuser.domain.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for user profile
 * Matches the UserProfileResponse schema in OpenAPI specification
 */
@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String avatar;
    private UserRole role;
    private String businessName;
    private String businessCode;
    private LocalDateTime createdAt;
    private boolean emailVerified;
    private boolean darkMode;
}
