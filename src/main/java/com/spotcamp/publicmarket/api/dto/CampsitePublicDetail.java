package com.spotcamp.publicmarket.api.dto;

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
public class CampsitePublicDetail {

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
    private List<CampsiteImageDto> images;

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
    public static class CampsiteImageDto {
        private Long id;
        private String url;
        private String caption;
        private Boolean isPrimary;
    }
}
