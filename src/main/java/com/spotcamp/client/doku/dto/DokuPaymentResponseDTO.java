package com.spotcamp.client.doku.dto;

/**
 * Canonical response payload from DOKU client boundary.
 */
public record DokuPaymentResponseDTO(
        String requestId,
        String status,
        String paymentUrl
) {
}
