package com.spotcamp.module.visualmap.dto;

import com.spotcamp.module.visualmap.entity.SpotDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapAvailabilityResponseDTO {

    private Long campsiteId;
    private Integer imageWidth;
    private Integer imageHeight;
    private String backgroundImageUrl;
    private List<SpotDefinition> spots;
    private Map<String, String> availability;
}
