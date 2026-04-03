package com.spotcamp.client.doku.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * Canonical outbound request payload to DOKU client boundary.
 */
@Builder
public record DokuPaymentRequestDTO(
        String referenceId,
        BigDecimal amount,
        String currency,
        String paymentMethod
) {
}
