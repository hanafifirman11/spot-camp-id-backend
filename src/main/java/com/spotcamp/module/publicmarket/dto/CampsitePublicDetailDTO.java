package com.spotcamp.module.publicmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for public campsite detail view
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsitePublicDetailDTO {

    private Long id;
    private String name;
    private String description;
    private Double latitude;
    private Double longitude;
    private BigDecimal price;
    private String image;
    private Double rating;
    private Integer reviews;
    private List<String> amenities;
    private List<String> rules;
    private MerchantInfo merchant;
    private List<CampsiteImageDTO> images;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MerchantInfo {
        private String name;
        private LocalDate joinedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampsiteImageDTO {
        private Long id;
        private String url;
        private String caption;
        private Boolean isPrimary;
    }
}
