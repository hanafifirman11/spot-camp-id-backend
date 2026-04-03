package com.spotcamp.module.campsite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityResponseDTO {

    private List<AmenityItem> amenities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AmenityItem {
        private Long id;
        private String code;
        private String name;
        private String icon;
        private String category;
    }

    public static AmenityResponseDTO of(List<AmenityItem> amenities) {
        return new AmenityResponseDTO(amenities);
    }
}
