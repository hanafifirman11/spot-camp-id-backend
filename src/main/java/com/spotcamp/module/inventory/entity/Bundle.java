package com.spotcamp.module.inventory.entity;

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
 * Bundle entity representing packaged deals
 * Combines multiple products with discount pricing
 */
@Entity
@Table(name = "bundles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bundle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "campsite_id", nullable = false)
    private Long campsiteId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "bundle_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal bundlePrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProductStatus status = ProductStatus.ACTIVE;

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @Builder.Default
    private List<BundleComponent> components = new ArrayList<>();

    /**
     * Checks if bundle is active
     */
    public boolean isActive() {
        return status == ProductStatus.ACTIVE;
    }

    /**
     * Adds a component to this bundle
     */
    public void addComponent(Product product, int quantity) {
        BundleComponent component = BundleComponent.builder()
                .bundle(this)
                .product(product)
                .quantity(quantity)
                .build();
        
        components.add(component);
    }

    /**
     * Calculates the total individual price of all components
     */
    public BigDecimal calculateIndividualPrice() {
        return components.stream()
                .map(component -> {
                    BigDecimal productPrice = component.getProduct().getEffectivePrice();
                    return productPrice.multiply(BigDecimal.valueOf(component.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Calculates the discount amount
     */
    public BigDecimal getDiscountAmount() {
        BigDecimal individualPrice = calculateIndividualPrice();
        return individualPrice.subtract(bundlePrice);
    }

    /**
     * Calculates the discount percentage
     */
    public BigDecimal getDiscountPercentage() {
        BigDecimal individualPrice = calculateIndividualPrice();
        if (individualPrice.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal discount = getDiscountAmount();
        return discount.divide(individualPrice, 4, BigDecimal.ROUND_HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Validates bundle data
     */
    public boolean isValid() {
        return name != null && !name.trim().isEmpty() &&
               bundlePrice != null && bundlePrice.compareTo(BigDecimal.ZERO) >= 0 &&
               components != null && components.size() >= 2 &&
               components.stream().allMatch(BundleComponent::isValid);
    }

    /**
     * Gets the total number of products in this bundle
     */
    public int getTotalQuantity() {
        return components.stream()
                .mapToInt(BundleComponent::getQuantity)
                .sum();
    }
}