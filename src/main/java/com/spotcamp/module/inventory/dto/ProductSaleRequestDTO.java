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
 * Request DTO for creating sale products
 * Matches the ProductSaleRequestDTO schema in OpenAPI specification
 */
@Data
public class ProductSaleRequestDTO {

    @NotNull(message = "Product type is required")
    private ProductType type = ProductType.SALE;

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

    @NotNull(message = "Current stock is required")
    @Min(value = 0, message = "Current stock cannot be negative")
    private Integer currentStock;

    @Min(value = 0, message = "Reorder level cannot be negative")
    private Integer reorderLevel = 10;

    @NotNull(message = "Unit price is required")
    @DecimalMin(value = "0.0", message = "Unit price must be non-negative")
    private BigDecimal unitPrice;

    @Size(max = 5, message = "Maximum 5 images allowed")
    private List<String> images;
}
