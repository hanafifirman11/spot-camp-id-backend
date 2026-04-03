package com.spotcamp.module.visualmap.dto;

import com.spotcamp.module.visualmap.entity.MapStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapSummaryResponseDTO {

    private Long id;
    private Long campsiteId;
    private String mapCode;
    private String mapName;
    private List<Long> productIds;
    private Integer imageWidth;
    private Integer imageHeight;
    private String backgroundImageUrl;
    private MapStatus status;
    private LocalDateTime updatedAt;
}
