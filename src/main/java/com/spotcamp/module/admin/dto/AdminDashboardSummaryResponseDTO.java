package com.spotcamp.module.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardSummaryResponseDTO {
    private long totalBusinesses;
    private long activeBusinesses;
    private long totalCampsites;
    private long activeCampsites;
    private long totalCampers;
    private long totalBookings;
    
    // Observability
    private Double systemCpuUsage;
    private Double processCpuUsage;
    private Long totalMemory;
    private Long freeMemory;
    private Long uptimeSeconds;
    private Long diskTotal;
    private Long diskFree;
    private Long httpRequestCount;
    private Long httpErrorCount;
    private Double httpAverageLatencyMs;
}
