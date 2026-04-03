package com.spotcamp.inventory.api.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for bundle definition responses
 * Matches the BundleDefinition schema in OpenAPI specification
 */
@Data
@Builder
public class BundleDefinitionDto {

    private Long id;
    private Long campsiteId;
    private String name;
    private String description;
    private BigDecimal bundlePrice;
    private List<BundleComponentDto> components;
    private LocalDateTime createdAt;

    @Data
    @Builder
    public static class BundleComponentDto {
        private Long productId;
        private String productName;
        private Integer quantity;
    }
}