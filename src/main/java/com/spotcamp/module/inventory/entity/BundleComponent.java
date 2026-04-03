package com.spotcamp.module.inventory.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Component of a bundle representing a product with quantity
 */
@Entity
@Table(name = "bundle_components")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BundleComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bundle_id", nullable = false)
    private Bundle bundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "quantity", nullable = false)
    @Builder.Default
    private Integer quantity = 1;

    /**
     * Validates component data
     */
    public boolean isValid() {
        return product != null && quantity != null && quantity > 0;
    }
}