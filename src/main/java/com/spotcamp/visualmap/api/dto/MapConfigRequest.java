package com.spotcamp.visualmap.api.dto;

import com.spotcamp.visualmap.domain.SpotDefinition;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.util.List;

/**
 * Request DTO for creating/updating map configuration
 * Matches the MapConfigRequest schema in OpenAPI specification
 */
@Data
public class MapConfigRequest {

    private String mapCode;

    @NotBlank(message = "Map name is required")
    private String mapName;

    @NotBlank(message = "Background image URL is required")
    @URL(message = "Invalid URL format")
    private String backgroundImageUrl;

    @NotNull(message = "Image width is required")
    @Min(value = 100, message = "Image width must be at least 100 pixels")
    private Integer imageWidth;

    @NotNull(message = "Image height is required")
    @Min(value = 100, message = "Image height must be at least 100 pixels")
    private Integer imageHeight;

    @NotEmpty(message = "At least one spot must be defined")
    @Size(max = 500, message = "Maximum 500 spots allowed")
    @Valid
    private List<SpotDefinition> spots;
}
