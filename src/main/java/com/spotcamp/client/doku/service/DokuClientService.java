package com.spotcamp.client.doku.service;

import com.spotcamp.client.doku.dto.DokuPaymentRequestDTO;
import com.spotcamp.client.doku.dto.DokuPaymentResponseDTO;

/**
 * Abstraction for DOKU payment gateway integration.
 *
 * Current codebase has no concrete outbound DOKU HTTP call yet;
 * this interface is introduced as the canonical integration boundary.
 */
public interface DokuClientService {

    DokuPaymentResponseDTO createPayment(DokuPaymentRequestDTO requestDTO);
}
