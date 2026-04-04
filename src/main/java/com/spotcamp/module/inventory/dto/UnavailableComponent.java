package com.spotcamp.module.inventory.dto;

import com.spotcamp.module.inventory.service.BundleService.UnavailabilityReason;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UnavailableComponent {
    private Long productId;
    private String productName;
    private UnavailabilityReason reason;
}
