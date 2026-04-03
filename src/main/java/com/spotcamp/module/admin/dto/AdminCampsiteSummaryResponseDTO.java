package com.spotcamp.module.admin.dto;

import com.spotcamp.module.campsite.entity.CampsiteStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCampsiteSummaryResponseDTO {
    private Long id;
    private String code;
    private String name;
    private String location;
    private String address;
    private CampsiteStatus status;
    private BigDecimal minPrice;
    private BigDecimal rating;
    private Integer reviewCount;
    private Long businessId;
    private String businessName;
    private String businessCode;
    private LocalDateTime createdAt;
}
