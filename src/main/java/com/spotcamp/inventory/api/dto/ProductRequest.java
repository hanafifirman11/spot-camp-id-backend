package com.spotcamp.inventory.api.dto;

import com.spotcamp.inventory.domain.ProductCategory;
import com.spotcamp.inventory.domain.ProductItemType;
import com.spotcamp.inventory.domain.ProductType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Generic product request DTO that handles both rental and sale products
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Type is required")
    private ProductType type;

    private ProductCategory category;

    private ProductItemType itemType;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    // For rental products
    private Integer stockTotal;
    private Integer bufferTimeMinutes;
    private BigDecimal dailyRate;

    // For sale products
    private Integer currentStock;
    private Integer reorderLevel;
    private BigDecimal unitPrice;

    private List<String> images;
}
