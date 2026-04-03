package com.spotcamp.visualmap.api.dto;

import com.spotcamp.visualmap.domain.SpotDefinition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MapConfigurationRequest {

    private String mapCode;

    @NotBlank(message = "Map name is required")
    private String mapName;

    @NotNull(message = "Image width is required")
    private Integer imageWidth;

    @NotNull(message = "Image height is required")
    private Integer imageHeight;

    private String backgroundImageUrl;

    @NotNull(message = "Spots are required")
    private List<SpotDefinition> spots;
}
