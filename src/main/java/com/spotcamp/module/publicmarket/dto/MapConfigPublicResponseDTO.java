package com.spotcamp.module.publicmarket.dto;

import com.spotcamp.module.visualmap.entity.SpotDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for public map configuration response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapConfigPublicResponseDTO {

    private Long id;
    private Long campsiteId;
    private String mapCode;
    private String mapName;
    private Integer imageWidth;
    private Integer imageHeight;
    private String backgroundImageUrl;
    private List<SpotDefinition> spots;
}
