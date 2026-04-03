package com.spotcamp.inventory.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.spotcamp.inventory.domain.ProductCategory;
import com.spotcamp.inventory.domain.ProductItemType;
import com.spotcamp.inventory.domain.ProductStatus;
import com.spotcamp.inventory.domain.ProductType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for product operations
 * Matches the ProductResponse schema in OpenAPI specification
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductResponse {

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
    private RentalDetailsDto rentalDetails;
    private SaleDetailsDto saleDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RentalDetailsDto {
        private Integer stockTotal;
        private Integer bufferTime;
        private BigDecimal dailyRate;
    }

    @Data
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SaleDetailsDto {
        private Integer currentStock;
        private Integer reorderLevel;
        private BigDecimal unitPrice;
    }
}
