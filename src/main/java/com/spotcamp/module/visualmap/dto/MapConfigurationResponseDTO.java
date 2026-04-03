package com.spotcamp.module.visualmap.dto;

import com.spotcamp.module.visualmap.entity.SpotDefinition;
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
public class MapConfigurationResponseDTO {

    private Long id;
    private Long campsiteId;
    private String mapCode;
    private String mapName;
    private Integer imageWidth;
    private Integer imageHeight;
    private String backgroundImageUrl;
    private List<SpotDefinition> spots;
    private Integer version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
