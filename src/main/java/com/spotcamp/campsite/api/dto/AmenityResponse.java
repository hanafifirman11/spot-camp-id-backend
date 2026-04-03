package com.spotcamp.campsite.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AmenityResponse {

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

    public static AmenityResponse of(List<AmenityItem> amenities) {
        return new AmenityResponse(amenities);
    }
}
