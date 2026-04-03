package com.spotcamp.inventory.domain;

import com.spotcamp.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Product entity representing both rental and sale items
 * Supports hybrid inventory model for campsites
 */
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private ProductType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "item_type", nullable = false)
    private ProductItemType itemType;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    // Rental specific fields
    @Column(name = "stock_total")
    private Integer stockTotal;

    @Column(name = "buffer_time_minutes")
    private Integer bufferTimeMinutes = 120;

    @Column(name = "daily_rate", precision = 12, scale = 2)
    private BigDecimal dailyRate;

    // Sale specific fields
    @Column(name = "current_stock")
    private Integer currentStock;

    @Column(name = "reorder_level")
    private Integer reorderLevel = 10;

    @Column(name = "unit_price", precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    /**
     * Checks if this product is a rental item
     */
    public boolean isRental() {
        return type == ProductType.RENTAL_SPOT || type == ProductType.RENTAL_ITEM;
    }

    /**
     * Checks if this product is a rental map spot
     */
    public boolean isRentalSpot() {
        return type == ProductType.RENTAL_SPOT;
    }

    /**
     * Checks if this product is a rental item
     */
    public boolean isRentalItem() {
        return type == ProductType.RENTAL_ITEM;
    }

    /**
     * Checks if this product is a sale item
     */
    public boolean isSale() {
        return type == ProductType.SALE;
    }

    /**
     * Checks if product is active
     */
    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    /**
     * Checks if rental product is available
     */
    public boolean isRentalAvailable() {
        return isRental() && isActive() && stockTotal != null && stockTotal > 0;
    }

    /**
     * Checks if sale product has sufficient stock
     */
    public boolean hasSufficientStock(int quantity) {
        if (!isSale() || !isActive()) {
            return false;
        }
        return currentStock != null && currentStock >= quantity;
    }

    /**
     * Checks if sale product needs reordering
     */
    public boolean needsReorder() {
        if (!isSale()) {
            return false;
        }
        return currentStock != null && reorderLevel != null && currentStock <= reorderLevel;
    }

    /**
     * Reduces stock for sale items
     */
    public void reduceStock(int quantity) {
        if (!isSale()) {
            throw new IllegalStateException("Cannot reduce stock for rental products");
        }
        
        if (!hasSufficientStock(quantity)) {
            throw new IllegalStateException("Insufficient stock available");
        }
        
        this.currentStock -= quantity;
    }

    /**
     * Increases stock for sale items
     */
    public void increaseStock(int quantity) {
        if (!isSale()) {
            throw new IllegalStateException("Cannot increase stock for rental products");
        }
        
        this.currentStock += quantity;
    }

    /**
     * Gets the effective price for this product
     */
    public BigDecimal getEffectivePrice() {
        if (isRental()) {
            return dailyRate != null ? dailyRate : basePrice;
        } else {
            return unitPrice != null ? unitPrice : basePrice;
        }
    }

    /**
     * Compatibility accessor for booking logic.
     */
    public BigDecimal getPrice() {
        return getEffectivePrice();
    }

    /**
     * Compatibility accessor for booking stock calculations.
     */
    public int getQuantity() {
        if (isRental()) {
            return stockTotal != null ? stockTotal : 0;
        }
        return currentStock != null ? currentStock : 0;
    }

    /**
     * Validates product data consistency
     */
    public boolean isValid() {
        if (category == null || itemType == null) {
            return false;
        }
        if (type == ProductType.RENTAL_SPOT) {
            return stockTotal != null && stockTotal > 0 &&
                   dailyRate != null && dailyRate.compareTo(BigDecimal.ZERO) > 0;
        } else if (type == ProductType.RENTAL_ITEM) {
            return stockTotal != null && stockTotal >= 0 &&
                   dailyRate != null && dailyRate.compareTo(BigDecimal.ZERO) > 0;
        } else if (type == ProductType.SALE) {
            return currentStock != null && currentStock >= 0 && 
                   unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0;
        }
        return false;
    }

    /**
     * Adds an image to this product
     */
    public void addImage(String imageUrl, int displayOrder) {
        ProductImage image = ProductImage.builder()
                .product(this)
                .imageUrl(imageUrl)
                .displayOrder(displayOrder)
                .build();
        
        images.add(image);
    }
}
