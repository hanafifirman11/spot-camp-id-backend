package com.spotcamp.inventory.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating/updating bundles
 * Matches the BundleRequest schema in OpenAPI specification
 */
@Data
public class BundleRequest {

    @NotNull(message = "Campsite ID is required")
    private Long campsiteId;

    @NotBlank(message = "Bundle name is required")
    @Size(max = 200, message = "Bundle name cannot exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Bundle price is required")
    @DecimalMin(value = "0.0", message = "Bundle price must be non-negative")
    private BigDecimal bundlePrice;

    @Valid
    @NotEmpty(message = "Bundle components are required")
    @Size(min = 2, max = 10, message = "Bundle must contain between 2 and 10 components")
    private List<BundleComponentRequest> components;

    @Data
    public static class BundleComponentRequest {

        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotNull(message = "Quantity is required")
        @jakarta.validation.constraints.Min(value = 1, message = "Quantity must be positive")
        private Integer quantity;
    }
}