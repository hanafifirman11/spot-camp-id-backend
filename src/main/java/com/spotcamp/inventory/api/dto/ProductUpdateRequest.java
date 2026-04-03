package com.spotcamp.inventory.api.dto;

import com.spotcamp.inventory.domain.ProductCategory;
import com.spotcamp.inventory.domain.ProductItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Product update request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductUpdateRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String status;

    private ProductCategory category;

    private ProductItemType itemType;

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
