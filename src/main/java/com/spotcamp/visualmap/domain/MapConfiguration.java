package com.spotcamp.visualmap.domain;

import com.spotcamp.common.audit.BaseEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

/**
 * Entity representing visual map configuration for campsites
 * Stores the map layout, background image, and spot definitions
 */
@Entity
@Table(name = "map_configurations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MapConfiguration extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "map_code", nullable = false, length = 20)
    private String mapCode;

    @Column(name = "map_name", nullable = false, length = 200)
    private String mapName;

    @Column(name = "background_image_url", nullable = false, length = 500)
    private String backgroundImageUrl;

    @Column(name = "image_width", nullable = false)
    private Integer imageWidth;

    @Column(name = "image_height", nullable = false)
    private Integer imageHeight;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "config_data", nullable = false)
    private MapConfigData configData;

    @Column(name = "version_number", nullable = false)
    private Integer versionNumber = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MapStatus status = MapStatus.DRAFT;

    /**
     * Checks if this configuration is active
     */
    public boolean isActive() {
        return status == MapStatus.ACTIVE;
    }

    /**
     * Activates this configuration
     */
    public void activate() {
        this.status = MapStatus.ACTIVE;
    }

    /**
     * Archives this configuration
     */
    public void archive() {
        this.status = MapStatus.ARCHIVED;
    }

    /**
     * Gets the number of spots defined in this configuration
     */
    public int getSpotCount() {
        return configData != null && configData.getSpots() != null ? 
               configData.getSpots().size() : 0;
    }

    /**
     * Validates the map configuration
     */
    public boolean isValid() {
        return mapCode != null && !mapCode.trim().isEmpty() &&
               mapName != null && !mapName.trim().isEmpty() &&
               backgroundImageUrl != null && !backgroundImageUrl.trim().isEmpty() &&
               imageWidth != null && imageWidth > 0 &&
               imageHeight != null && imageHeight > 0 &&
               configData != null &&
               configData.getSpots() != null &&
               !configData.getSpots().isEmpty();
    }
}
