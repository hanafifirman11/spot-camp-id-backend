package com.spotcamp.module.booking.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerifyPaymentRequestDTO {
    @NotNull(message = "Approval decision is required")
    private Boolean approved;

    @Size(max = 500, message = "Note must not exceed 500 characters")
    private String note;

    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }
}
