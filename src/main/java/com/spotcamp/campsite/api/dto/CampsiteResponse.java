package com.spotcamp.campsite.api.dto;

import com.spotcamp.campsite.domain.CampsiteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampsiteResponse {

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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
