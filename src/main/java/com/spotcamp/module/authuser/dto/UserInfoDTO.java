package com.spotcamp.module.authuser.dto;

import com.spotcamp.module.authuser.entity.UserRole;
import lombok.Builder;
import lombok.Data;

/**
 * DTO for user information in auth responses
 * Matches the UserInfo schema in OpenAPI specification
 */
@Data
@Builder
public class UserInfoDTO {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private UserRole role;
    private String businessName;
    private String businessCode;
    private boolean darkMode;
}
