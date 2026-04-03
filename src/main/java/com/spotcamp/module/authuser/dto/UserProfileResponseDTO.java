package com.spotcamp.module.authuser.dto;

import com.spotcamp.module.authuser.entity.UserRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for user profile
 * Matches the UserProfileResponseDTO schema in OpenAPI specification
 */
@Data
@Builder
public class UserProfileResponseDTO {

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
