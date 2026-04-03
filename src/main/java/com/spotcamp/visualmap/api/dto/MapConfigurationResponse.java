package com.spotcamp.visualmap.api.dto;

import com.spotcamp.visualmap.domain.SpotDefinition;
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
public class MapConfigurationResponse {

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
