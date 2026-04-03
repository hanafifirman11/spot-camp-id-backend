package com.spotcamp.module.campsite.dto;

import com.spotcamp.module.campsite.entity.CampsiteStatus;
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
public class CampsiteDetailResponseDTO {

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
    private List<CampsiteImageDTO> images;
    private List<AmenityDTO> amenities;
    private List<String> rules;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CampsiteImageDTO {
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
    public static class AmenityDTO {
        private Long id;
        private String code;
        private String name;
        private String icon;
        private String category;
    }
}
