package com.spotcamp.module.inventory.repository;

import com.spotcamp.module.inventory.entity.Bundle;
import com.spotcamp.module.inventory.entity.ProductStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Bundle entity operations
 */
@Repository
public interface BundleRepository extends JpaRepository<Bundle, Long> {

    /**
     * Find bundles by campsite with pagination
     */
    @EntityGraph(attributePaths = {"components", "components.product"})
    Page<Bundle> findByCampsiteIdAndStatusOrderByNameAsc(Long campsiteId, ProductStatus status, Pageable pageable);

    /**
     * Find active bundles by campsite
     */
    @EntityGraph(attributePaths = {"components", "components.product"})
    List<Bundle> findByCampsiteIdAndStatusOrderByNameAsc(Long campsiteId, ProductStatus status);

    /**
     * Find all active bundles
     */
    @EntityGraph(attributePaths = {"components", "components.product"})
    List<Bundle> findByStatusOrderByNameAsc(ProductStatus status);

    /**
     * Count bundles by campsite and status
     */
    long countByCampsiteIdAndStatus(Long campsiteId, ProductStatus status);

    /**
     * Find bundles containing a specific product
     */
    @Query("SELECT DISTINCT b FROM Bundle b JOIN b.components c WHERE c.product.id = :productId AND b.status = :status")
    List<Bundle> findBundlesContainingProduct(@Param("productId") Long productId, @Param("status") ProductStatus status);

    /**
     * Check if bundle exists and is active
     */
    boolean existsByIdAndStatus(Long id, ProductStatus status);

    /**
     * Search bundles by name or description
     */
    @Query("SELECT b FROM Bundle b WHERE " +
           "b.status = 'ACTIVE' AND " +
           "(LOWER(b.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    @EntityGraph(attributePaths = {"components", "components.product"})
    Page<Bundle> searchBundles(@Param("searchTerm") String searchTerm, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"components", "components.product"})
    java.util.Optional<Bundle> findById(Long id);
}
