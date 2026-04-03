package com.spotcamp.module.inventory.repository;

import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.entity.ProductStatus;
import com.spotcamp.module.inventory.entity.ProductType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity operations
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by campsite with filters
     */
    @Query("SELECT p FROM Product p WHERE " +
           "(:campsiteId IS NULL OR p.campsiteId = :campsiteId) AND " +
           "(:type IS NULL OR p.type = :type) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:query IS NULL OR " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(COALESCE(p.description, '')) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> findByFilters(@Param("campsiteId") Long campsiteId,
                               @Param("type") ProductType type,
                               @Param("status") ProductStatus status,
                               @Param("query") String query,
                               Pageable pageable);

    /**
     * Find active products by campsite
     */
    List<Product> findByCampsiteIdAndStatusOrderByNameAsc(Long campsiteId, ProductStatus status);

    /**
     * Find rental products by campsite
     */
    @Query("SELECT p FROM Product p WHERE p.campsiteId = :campsiteId AND " +
           "p.type IN ('RENTAL_SPOT', 'RENTAL_ITEM') AND p.status = :status")
    List<Product> findRentalProductsByCampsite(@Param("campsiteId") Long campsiteId, 
                                              @Param("status") ProductStatus status);

    /**
     * Find sale products by campsite
     */
    @Query("SELECT p FROM Product p WHERE p.campsiteId = :campsiteId AND p.type = 'SALE' AND p.status = :status")
    List<Product> findSaleProductsByCampsite(@Param("campsiteId") Long campsiteId, 
                                            @Param("status") ProductStatus status);

    /**
     * Find products that need reordering
     */
    @Query("SELECT p FROM Product p WHERE p.type = 'SALE' AND p.status = 'ACTIVE' AND " +
           "p.currentStock <= p.reorderLevel AND p.campsiteId = :campsiteId")
    List<Product> findProductsNeedingReorder(@Param("campsiteId") Long campsiteId);

    /**
     * Find products with low stock across all campsites
     */
    @Query("SELECT p FROM Product p WHERE p.type = 'SALE' AND p.status = 'ACTIVE' AND " +
           "p.currentStock <= p.reorderLevel")
    List<Product> findAllProductsNeedingReorder();

    /**
     * Update stock for sale product
     */
    @Modifying
    @Query("UPDATE Product p SET p.currentStock = p.currentStock - :quantity WHERE " +
           "p.id = :productId AND p.type = 'SALE' AND p.currentStock >= :quantity")
    int reduceStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Update stock for sale product (increase)
     */
    @Modifying
    @Query("UPDATE Product p SET p.currentStock = p.currentStock + :quantity WHERE " +
           "p.id = :productId AND p.type = 'SALE'")
    int increaseStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    /**
     * Count products by type and campsite
     */
    long countByCampsiteIdAndType(Long campsiteId, ProductType type);

    /**
     * Count products by status and campsite
     */
    long countByCampsiteIdAndStatus(Long campsiteId, ProductStatus status);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE " +
           "p.status = 'ACTIVE' AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find products by IDs (for bundle validation)
     */
    @Query("SELECT p FROM Product p WHERE p.id IN :productIds AND p.status = 'ACTIVE'")
    List<Product> findActiveProductsByIds(@Param("productIds") List<Long> productIds);

    /**
     * Find a product with pessimistic write lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :productId")
    Optional<Product> findByIdForUpdate(@Param("productId") Long productId);

    /**
     * Check if product exists and is active
     */
    boolean existsByIdAndStatus(Long id, ProductStatus status);
}
