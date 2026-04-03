package com.spotcamp.module.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request for updating product stock
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStockRequestDTO {

    @NotNull(message = "Adjustment is required")
    private Integer adjustment;

    private String reason;
}
