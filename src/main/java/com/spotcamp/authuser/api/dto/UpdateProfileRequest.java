package com.spotcamp.authuser.api.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating user profile
 * Matches the UpdateProfileRequest schema in OpenAPI specification
 */
@Data
public class UpdateProfileRequest {

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "Invalid phone number format (use E.164 format)"
    )
    private String phone;

    private String avatar;

    private Boolean darkMode;
}
