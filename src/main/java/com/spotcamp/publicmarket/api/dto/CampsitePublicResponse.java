package com.spotcamp.publicmarket.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO for public campsite listing in marketplace
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsitePublicResponse {

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
}
