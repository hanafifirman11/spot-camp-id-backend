package com.spotcamp.module.visualmap.repository;

import com.spotcamp.module.visualmap.entity.MapConfiguration;
import com.spotcamp.module.visualmap.entity.MapStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for MapConfiguration entity operations
 */
@Repository
public interface MapConfigurationRepository extends JpaRepository<MapConfiguration, Long> {

    /**
     * Find the active map configuration for a campsite
     */
    Optional<MapConfiguration> findFirstByCampsiteIdAndStatusOrderByUpdatedAtDesc(Long campsiteId, MapStatus status);

    List<MapConfiguration> findByCampsiteIdAndStatusOrderByUpdatedAtDesc(Long campsiteId, MapStatus status);

    Optional<MapConfiguration> findByCampsiteIdAndMapCodeAndStatus(Long campsiteId, String mapCode, MapStatus status);

    boolean existsByCampsiteIdAndMapCode(Long campsiteId, String mapCode);

    /**
     * Find all configurations for a campsite ordered by version
     */
    @Query("SELECT mc FROM MapConfiguration mc WHERE mc.campsiteId = :campsiteId ORDER BY mc.versionNumber DESC")
    List<MapConfiguration> findByCampsiteIdOrderByVersionDesc(@Param("campsiteId") Long campsiteId);

    /**
     * Get the latest version number for a campsite
     */
    @Query("""
        SELECT COALESCE(MAX(mc.versionNumber), 0)
        FROM MapConfiguration mc
        WHERE mc.campsiteId = :campsiteId AND mc.mapCode = :mapCode
        """)
    Integer getLatestVersionNumber(@Param("campsiteId") Long campsiteId, @Param("mapCode") String mapCode);

    /**
     * Archive all active configurations for a campsite (when activating a new one)
     */
    @Modifying
    @Query("""
        UPDATE MapConfiguration mc
        SET mc.status = 'ARCHIVED'
        WHERE mc.campsiteId = :campsiteId
          AND mc.mapCode = :mapCode
          AND mc.status = 'ACTIVE'
        """)
    void archiveActiveConfigurations(@Param("campsiteId") Long campsiteId, @Param("mapCode") String mapCode);

    /**
     * Check if campsite has any configuration
     */
    boolean existsByCampsiteId(Long campsiteId);

    /**
     * Count configurations by status
     */
    long countByStatus(MapStatus status);

    /**
     * Find configurations by status (for admin purposes)
     */
    List<MapConfiguration> findByStatusOrderByUpdatedAtDesc(MapStatus status);
}
