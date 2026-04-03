package com.spotcamp.visualmap.service;

import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.common.util.CodeGenerator;
import com.spotcamp.visualmap.domain.MapConfiguration;
import com.spotcamp.visualmap.domain.MapConfigData;
import com.spotcamp.visualmap.domain.MapStatus;
import com.spotcamp.visualmap.domain.SpotDefinition;
import com.spotcamp.visualmap.repository.MapConfigurationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Service for map configuration operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MapConfigurationService {

    private final MapConfigurationRepository mapConfigRepository;
    private static final int CODE_LENGTH = 8;

    /**
     * Get the latest active map configuration for a campsite
     */
    @Cacheable(value = "mapConfig", key = "#campsiteId")
    public MapConfiguration getActiveConfiguration(Long campsiteId) {
        log.debug("Getting active map configuration for campsite: {}", campsiteId);

        return mapConfigRepository.findFirstByCampsiteIdAndStatusOrderByUpdatedAtDesc(campsiteId, MapStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active map configuration", campsiteId));
    }

    /**
     * Get all active map configurations for a campsite
     */
    public List<MapConfiguration> getActiveConfigurations(Long campsiteId) {
        log.debug("Getting active map configurations for campsite: {}", campsiteId);
        return mapConfigRepository.findByCampsiteIdAndStatusOrderByUpdatedAtDesc(campsiteId, MapStatus.ACTIVE);
    }

    /**
     * Get map configuration by ID
     */
    public MapConfiguration getConfiguration(Long configId) {
        return mapConfigRepository.findById(configId)
                .orElseThrow(() -> new ResourceNotFoundException("Map configuration", configId));
    }

    /**
     * Get a map configuration scoped to a campsite
     */
    public MapConfiguration getConfigurationForCampsite(Long campsiteId, Long configId) {
        MapConfiguration configuration = getConfiguration(configId);
        if (!configuration.getCampsiteId().equals(campsiteId)) {
            throw new BusinessException("Configuration does not belong to the specified campsite");
        }
        return configuration;
    }

    /**
     * Get an active map configuration scoped to a campsite
     */
    public MapConfiguration getActiveConfigurationForCampsite(Long campsiteId, Long configId) {
        MapConfiguration configuration = getConfigurationForCampsite(campsiteId, configId);
        if (configuration.getStatus() != MapStatus.ACTIVE) {
            throw new ResourceNotFoundException("Active map configuration", configId);
        }
        return configuration;
    }

    /**
     * Get an active map configuration by map code
     */
    public MapConfiguration getActiveConfigurationByCode(Long campsiteId, String mapCode) {
        if (mapCode == null || mapCode.trim().isEmpty()) {
            throw new ValidationException("mapCode", "Map code is required");
        }
        String normalized = mapCode.trim().toUpperCase();
        return mapConfigRepository.findByCampsiteIdAndMapCodeAndStatus(campsiteId, normalized, MapStatus.ACTIVE)
                .orElseThrow(() -> new ResourceNotFoundException("Active map configuration", normalized));
    }

    /**
     * Create or update map configuration
     */
    @Transactional
    @CacheEvict(value = "mapConfig", key = "#campsiteId")
    public MapConfiguration saveConfiguration(Long campsiteId, String mapCode, String mapName, String backgroundImageUrl,
                                            Integer imageWidth, Integer imageHeight,
                                            List<SpotDefinition> spots) {
        
        log.info("Saving map configuration for campsite: {}", campsiteId);
        
        // Validate input
        validateConfigurationData(mapName, backgroundImageUrl, imageWidth, imageHeight, spots);
        String normalizedMapName = mapName.trim();
        String normalizedMapCode = resolveMapCode(campsiteId, mapCode);
        validateUniqueProducts(campsiteId, normalizedMapCode, spots);
        
        // Create map config data
        MapConfigData configData = MapConfigData.builder()
                .spots(spots)
                .build();
        
        // Get next version number
        Integer nextVersion = mapConfigRepository.getLatestVersionNumber(campsiteId, normalizedMapCode) + 1;
        
        // Create new configuration
        MapConfiguration configuration = MapConfiguration.builder()
                .campsiteId(campsiteId)
                .mapCode(normalizedMapCode)
                .mapName(normalizedMapName)
                .backgroundImageUrl(backgroundImageUrl)
                .imageWidth(imageWidth)
                .imageHeight(imageHeight)
                .configData(configData)
                .versionNumber(nextVersion)
                .status(MapStatus.DRAFT)
                .build();
        
        MapConfiguration saved = mapConfigRepository.save(configuration);
        log.info("Map configuration saved with ID: {} for campsite: {}", saved.getId(), campsiteId);
        
        return saved;
    }

    /**
     * Activate a map configuration
     */
    @Transactional
    @CacheEvict(value = "mapConfig", key = "#campsiteId")
    public MapConfiguration activateConfiguration(Long campsiteId, Long configId) {
        log.info("Activating map configuration: {} for campsite: {}", configId, campsiteId);
        
        MapConfiguration configuration = getConfigurationForCampsite(campsiteId, configId);
        
        // Validate configuration before activation
        if (!configuration.isValid()) {
            throw new BusinessException("Cannot activate invalid map configuration");
        }

        validateUniqueProducts(campsiteId, configuration.getMapCode(), configuration.getConfigData().getSpots());
        
        // Archive active configuration for the same map name
        mapConfigRepository.archiveActiveConfigurations(campsiteId, configuration.getMapCode());
        
        // Activate the new configuration
        configuration.activate();
        MapConfiguration activated = mapConfigRepository.save(configuration);
        
        log.info("Map configuration activated: {} for campsite: {}", configId, campsiteId);
        return activated;
    }

    /**
     * Get all configurations for a campsite
     */
    public List<MapConfiguration> getConfigurationHistory(Long campsiteId) {
        return mapConfigRepository.findByCampsiteIdOrderByVersionDesc(campsiteId);
    }

    /**
     * Delete a draft configuration
     */
    @Transactional
    public void deleteConfiguration(Long campsiteId, Long configId) {
        log.info("Deleting map configuration: {} for campsite: {}", configId, campsiteId);
        
        MapConfiguration configuration = getConfiguration(configId);
        
        // Verify the configuration belongs to the campsite
        if (!configuration.getCampsiteId().equals(campsiteId)) {
            throw new BusinessException("Configuration does not belong to the specified campsite");
        }
        
        // Only allow deletion of draft configurations
        if (configuration.getStatus() != MapStatus.DRAFT) {
            throw new BusinessException("Can only delete draft configurations");
        }
        
        mapConfigRepository.delete(configuration);
        log.info("Map configuration deleted: {} for campsite: {}", configId, campsiteId);
    }

    /**
     * Check if a campsite has any map configuration
     */
    public boolean hasConfiguration(Long campsiteId) {
        return mapConfigRepository.existsByCampsiteId(campsiteId);
    }

    /**
     * Validate configuration data
     */
    private void validateConfigurationData(String mapName, String backgroundImageUrl, Integer imageWidth,
                                         Integer imageHeight, List<SpotDefinition> spots) {
        
        if (mapName == null || mapName.trim().isEmpty()) {
            throw new ValidationException("mapName", "Map name is required");
        }

        if (backgroundImageUrl == null || backgroundImageUrl.trim().isEmpty()) {
            throw new ValidationException("backgroundImageUrl", "Background image URL is required");
        }
        
        if (imageWidth == null || imageWidth <= 0) {
            throw new ValidationException("imageWidth", "Image width must be positive");
        }
        
        if (imageHeight == null || imageHeight <= 0) {
            throw new ValidationException("imageHeight", "Image height must be positive");
        }
        
        if (spots == null || spots.isEmpty()) {
            throw new ValidationException("spots", "At least one spot must be defined");
        }
        
        if (spots.size() > 500) {
            throw new ValidationException("spots", "Maximum 500 spots allowed");
        }
        
        // Validate each spot
        Set<String> spotIds = new HashSet<>();
        Set<Long> productIds = new HashSet<>();
        
        for (int i = 0; i < spots.size(); i++) {
            SpotDefinition spot = spots.get(i);
            
            if (!spot.isValid()) {
                throw new ValidationException("spots[" + i + "]", "Invalid spot definition");
            }
            
            // Check for duplicate spot IDs
            if (!spotIds.add(spot.getId())) {
                throw new ValidationException("spots[" + i + "].id", "Duplicate spot ID: " + spot.getId());
            }
            
            // Check for duplicate product IDs
            if (!productIds.add(spot.getProductId())) {
                throw new ValidationException("spots[" + i + "].productId", "Duplicate product ID: " + spot.getProductId());
            }
        }
    }

    private void validateUniqueProducts(Long campsiteId, String mapCode, List<SpotDefinition> spots) {
        if (spots == null || spots.isEmpty()) {
            return;
        }

        Set<Long> requestedProductIds = spots.stream()
                .map(SpotDefinition::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (requestedProductIds.isEmpty()) {
            return;
        }

        Set<Long> usedProductIds = new HashSet<>();
        List<MapConfiguration> activeConfigs = mapConfigRepository.findByCampsiteIdAndStatusOrderByUpdatedAtDesc(
                campsiteId, MapStatus.ACTIVE);
        for (MapConfiguration config : activeConfigs) {
            if (config.getMapCode() != null && config.getMapCode().equalsIgnoreCase(mapCode)) {
                continue;
            }
            MapConfigData data = config.getConfigData();
            if (data == null || data.getSpots() == null) {
                continue;
            }
            for (SpotDefinition spot : data.getSpots()) {
                if (spot.getProductId() != null) {
                    usedProductIds.add(spot.getProductId());
                }
            }
        }

        Set<Long> duplicates = requestedProductIds.stream()
                .filter(usedProductIds::contains)
                .collect(Collectors.toCollection(HashSet::new));
        if (!duplicates.isEmpty()) {
            throw new ValidationException(
                    "spots",
                    "Some products are already used in another map layout: " + duplicates
            );
        }
    }

    private String resolveMapCode(Long campsiteId, String mapCode) {
        if (mapCode != null && !mapCode.trim().isEmpty()) {
            return mapCode.trim().toUpperCase();
        }
        return CodeGenerator.generateUnique(
                "MAP",
                CODE_LENGTH,
                candidate -> mapConfigRepository.existsByCampsiteIdAndMapCode(campsiteId, candidate)
        );
    }
}
