package com.spotcamp.booking.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for checkout process
 */
@Data
public class CheckoutRequest {
    
    @NotBlank(message = "Contact name is required")
    @Size(min = 2, max = 200, message = "Contact name must be between 2 and 200 characters")
    private String contactName;
    
    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    private String contactEmail;
    
    @NotBlank(message = "Contact phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String contactPhone;
    
    @Size(max = 1000, message = "Special requests must not exceed 1000 characters")
    private String specialRequests;
    
    @NotBlank(message = "Payment method is required")
    @Pattern(regexp = "^(MANUAL_TRANSFER|DOKU_WALLET|DOKU_BANK|DOKU_CARD)$", message = "Invalid payment method")
    private String paymentMethod;

    @Pattern(regexp = "^(BCA|BRI|BNI|MANDIRI|PERMATA|CIMB)$", message = "Invalid payment channel")
    private String paymentChannel;
}
