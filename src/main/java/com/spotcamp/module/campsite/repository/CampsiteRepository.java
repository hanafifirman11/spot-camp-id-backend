package com.spotcamp.module.campsite.repository;

import com.spotcamp.module.campsite.entity.Campsite;
import com.spotcamp.module.campsite.entity.CampsiteStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampsiteRepository extends JpaRepository<Campsite, Long> {

    /**
     * Find campsites by merchant (owner) ID
     */
    Page<Campsite> findByOwnerId(Long ownerId, Pageable pageable);

    /**
     * Find campsites by merchant ID and status
     */
    Page<Campsite> findByOwnerIdAndStatus(Long ownerId, CampsiteStatus status, Pageable pageable);

    /**
     * Find all active campsites for public listing
     */
    Page<Campsite> findByStatus(CampsiteStatus status, Pageable pageable);

    /**
     * Search campsites by name, description or location (full-text search)
     */
    @Query("""
        SELECT c FROM Campsite c
        WHERE c.status = :status
        AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(c.description) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Campsite> searchByQuery(@Param("query") String query, @Param("status") CampsiteStatus status, Pageable pageable);

    /**
     * Admin search across all campsites with optional filters.
     */
    @Query("""
        SELECT c FROM Campsite c
        WHERE (:status IS NULL OR c.status = :status)
        AND (:ownerId IS NULL OR c.ownerId = :ownerId)
        AND (
            :query IS NULL
            OR LOWER(c.name) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.location) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.address) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(c.code) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        """)
    Page<Campsite> searchAdmin(@Param("query") String query,
                               @Param("status") CampsiteStatus status,
                               @Param("ownerId") Long ownerId,
                               Pageable pageable);

    /**
     * Find campsite by ID with status check
     */
    Optional<Campsite> findByIdAndStatus(Long id, CampsiteStatus status);

    /**
     * Find campsite by ID and owner ID
     */
    Optional<Campsite> findByIdAndOwnerId(Long id, Long ownerId);

    /**
     * Count campsites by owner
     */
    long countByOwnerId(Long ownerId);

    /**
     * Count campsites by owner and status
     */
    long countByOwnerIdAndStatus(Long ownerId, CampsiteStatus status);

    /**
     * Count campsites by status
     */
    long countByStatus(CampsiteStatus status);

    /**
     * Check if campsite code exists
     */
    boolean existsByCode(String code);

    /**
     * Find nearby campsites by coordinates
     */
    @Query(value = """
        SELECT * FROM campsites c
        WHERE c.status = 'ACTIVE'
        AND (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude))
             * cos(radians(c.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(c.latitude)))) < :radiusKm
        ORDER BY (6371 * acos(cos(radians(:lat)) * cos(radians(c.latitude))
             * cos(radians(c.longitude) - radians(:lng))
             + sin(radians(:lat)) * sin(radians(c.latitude)))) ASC
        """, nativeQuery = true)
    List<Campsite> findNearbyActive(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radiusKm") Double radiusKm
    );
}
