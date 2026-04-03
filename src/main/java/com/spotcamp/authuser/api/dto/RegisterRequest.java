package com.spotcamp.authuser.api.dto;

import com.spotcamp.authuser.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for user registration
 * Matches the RegisterRequest schema in OpenAPI specification
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,}$",
        message = "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, and one number"
    )
    private String password;

    @NotBlank(message = "First name is required")
    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Pattern(
        regexp = "^\\+?[1-9]\\d{1,14}$",
        message = "Invalid phone number format (use E.164 format)"
    )
    private String phone;

    @NotNull(message = "Role is required")
    private UserRole role;

    @Size(max = 200, message = "Business name cannot exceed 200 characters")
    private String businessName;
}