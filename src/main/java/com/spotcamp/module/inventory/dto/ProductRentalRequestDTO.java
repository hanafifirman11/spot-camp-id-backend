package com.spotcamp.module.inventory.dto;

import com.spotcamp.module.inventory.entity.ProductCategory;
import com.spotcamp.module.inventory.entity.ProductItemType;
import com.spotcamp.module.inventory.entity.ProductType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * Request DTO for creating rental products
 * Matches the ProductRentalRequestDTO schema in OpenAPI specification
 */
@Data
public class ProductRentalRequestDTO {

    @NotNull(message = "Product type is required")
    private ProductType type = ProductType.RENTAL_SPOT;

    private ProductCategory category;

    private ProductItemType itemType;

    @NotNull(message = "Campsite ID is required")
    private Long campsiteId;

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Product name cannot exceed 200 characters")
    private String name;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    @NotNull(message = "Base price is required")
    @DecimalMin(value = "0.0", message = "Base price must be non-negative")
    private BigDecimal basePrice;

    @NotNull(message = "Stock total is required")
    @Min(value = 1, message = "Stock total must be at least 1")
    private Integer stockTotal;

    @Min(value = 0, message = "Buffer time cannot be negative")
    private Integer bufferTime = 120;

    @NotNull(message = "Daily rate is required")
    @DecimalMin(value = "0.0", message = "Daily rate must be non-negative")
    private BigDecimal dailyRate;

    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> images;
}
