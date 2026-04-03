package com.spotcamp.module.inventory.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotcamp.module.inventory.entity.ProductCategory;
import com.spotcamp.module.inventory.entity.ProductItemType;
import com.spotcamp.module.inventory.entity.ProductStatus;
import com.spotcamp.module.inventory.entity.ProductType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for product operations
 * Matches the ProductResponseDTO schema in OpenAPI specification
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponseDTO {

    private Long id;
    private ProductType type;
    private ProductCategory category;
    private ProductItemType itemType;
    private Long campsiteId;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private List<String> images;
    private ProductStatus status;
    private RentalDetailsDTO rentalDetails;
    private SaleDetailsDTO saleDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RentalDetailsDTO {
        private Integer stockTotal;
        private Integer bufferTime;
        private BigDecimal dailyRate;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SaleDetailsDTO {
        private Integer currentStock;
        private Integer reorderLevel;
        private BigDecimal unitPrice;
    }
}
