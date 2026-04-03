package com.spotcamp.campsite.repository;

import com.spotcamp.campsite.domain.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    /**
     * Find all active amenities
     */
    List<Amenity> findByIsActiveTrue();

    /**
     * Find amenity by code
     */
    Optional<Amenity> findByCode(String code);

    /**
     * Find amenities by codes
     */
    List<Amenity> findByCodeIn(Set<String> codes);

    /**
     * Find amenities by campsite ID
     */
    @Query("""
        SELECT a FROM Amenity a
        JOIN a.campsites c
        WHERE c.id = :campsiteId
        """)
    List<Amenity> findByCampsiteId(@Param("campsiteId") Long campsiteId);

    /**
     * Check if amenity code exists
     */
    boolean existsByCode(String code);
}
