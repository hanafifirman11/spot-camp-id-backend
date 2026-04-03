package com.spotcamp.module.campsite.repository;

import com.spotcamp.module.campsite.entity.CampsiteImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampsiteImageRepository extends JpaRepository<CampsiteImage, Long> {

    /**
     * Find all images for a campsite
     */
    List<CampsiteImage> findByCampsiteIdOrderByDisplayOrderAsc(Long campsiteId);

    /**
     * Find primary image for a campsite
     */
    Optional<CampsiteImage> findByCampsiteIdAndIsPrimaryTrue(Long campsiteId);

    /**
     * Count images for a campsite
     */
    long countByCampsiteId(Long campsiteId);

    /**
     * Reset all primary flags for a campsite
     */
    @Modifying
    @Query("UPDATE CampsiteImage ci SET ci.isPrimary = false WHERE ci.campsiteId = :campsiteId")
    void resetPrimaryForCampsite(@Param("campsiteId") Long campsiteId);

    /**
     * Delete all images for a campsite
     */
    void deleteByCampsiteId(Long campsiteId);
}
