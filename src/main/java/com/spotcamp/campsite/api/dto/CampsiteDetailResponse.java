package com.spotcamp.campsite.api.dto;

import com.spotcamp.campsite.domain.CampsiteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteDetailResponse {

    private Long id;
    private String code;
    private String name;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private BigDecimal basePrice;
    private String coverImageUrl;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String contactEmail;
    private String contactPhone;
    private CampsiteStatus status;
    private Double rating;
    private Integer reviewCount;
    private List<CampsiteImageDto> images;
    private List<AmenityDto> amenities;
    private List<String> rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampsiteImageDto {
        private Long id;
        private String url;
        private String caption;
        private Boolean isPrimary;
        private Integer displayOrder;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmenityDto {
        private Long id;
        private String code;
        private String name;
        private String icon;
        private String category;
    }
}
