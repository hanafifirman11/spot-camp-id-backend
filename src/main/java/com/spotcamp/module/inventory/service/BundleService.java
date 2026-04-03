package com.spotcamp.module.inventory.service;

import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.common.util.PageUtils;
import com.spotcamp.module.inventory.entity.Bundle;
import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.entity.ProductStatus;
import com.spotcamp.module.inventory.repository.BundleRepository;
import com.spotcamp.module.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for bundle management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BundleService {

    private final BundleRepository bundleRepository;
    private final ProductRepository productRepository;
    private final ProductStockService productStockService;

    /**
     * Get bundles with pagination
     */
    @Cacheable(value = "bundles", key = "#campsiteId + ':' + #page + ':' + #size")
    public Page<Bundle> getBundles(Long campsiteId, Integer page, Integer size) {
        log.debug("Getting bundles for campsite: {}", campsiteId);
        
        Pageable pageable = PageUtils.createPageable(page, size, null);
        
        if (campsiteId != null) {
            return bundleRepository.findByCampsiteIdAndStatusOrderByNameAsc(
                    campsiteId, ProductStatus.ACTIVE, pageable);
        } else {
            // For public listing, return all active bundles with pagination
            return bundleRepository.findByStatusOrderByNameAsc(ProductStatus.ACTIVE)
                    .stream()
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            list -> {
                                int start = (int) pageable.getOffset();
                                int end = Math.min((start + pageable.getPageSize()), list.size());
                                return new org.springframework.data.domain.PageImpl<>(
                                        list.subList(start, end), pageable, list.size());
                            }
                    ));
        }
    }

    /**
     * Get bundle by ID
     */
    public Bundle getBundle(Long bundleId) {
        return bundleRepository.findById(bundleId)
                .orElseThrow(() -> new ResourceNotFoundException("Bundle", bundleId));
    }

    /**
     * Get active bundles by campsite
     */
    @Cacheable(value = "campsiteBundles", key = "#campsiteId")
    public List<Bundle> getActiveBundlesByCampsite(Long campsiteId) {
        return bundleRepository.findByCampsiteIdAndStatusOrderByNameAsc(campsiteId, ProductStatus.ACTIVE);
    }

    /**
     * Create a new bundle
     */
    @Transactional
    @CacheEvict(value = {"bundles", "campsiteBundles"}, key = "#campsiteId")
    public Bundle createBundle(Long campsiteId, String name, String description, 
                             BigDecimal bundlePrice, List<BundleComponentRequest> components) {
        
        log.info("Creating new bundle: {} for campsite: {}", name, campsiteId);
        
        // Validate input
        validateBundleData(name, bundlePrice, components);
        
        // Verify all products exist and are active
        List<Long> productIds = components.stream()
                .map(BundleComponentRequest::getProductId)
                .collect(Collectors.toList());
        
        List<Product> products = productRepository.findActiveProductsByIds(productIds);
        
        if (products.size() != productIds.size()) {
            throw new ValidationException("Some products in the bundle are not found or inactive");
        }
        
        // Verify all products belong to the same campsite
        boolean allProductsFromSameCampsite = products.stream()
                .allMatch(product -> product.getCampsiteId().equals(campsiteId));
        
        if (!allProductsFromSameCampsite) {
            throw new ValidationException("All products in a bundle must belong to the same campsite");
        }

        if (products.stream().anyMatch(product -> !product.isRentalItem())) {
            throw new ValidationException("Bundle components must be rental items");
        }
        
        // Create bundle
        Bundle bundle = Bundle.builder()
                .campsiteId(campsiteId)
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .bundlePrice(bundlePrice)
                .status(ProductStatus.ACTIVE)
                .build();
        
        // Add components
        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product));
        
        for (BundleComponentRequest componentReq : components) {
            Product product = productMap.get(componentReq.getProductId());
            bundle.addComponent(product, componentReq.getQuantity());
        }
        
        // Validate bundle
        if (!bundle.isValid()) {
            throw new ValidationException("Bundle data is invalid");
        }
        validateBundleDiscount(bundle);
        
        Bundle savedBundle = bundleRepository.save(bundle);
        log.info("Bundle created with ID: {}", savedBundle.getId());
        
        return savedBundle;
    }

    /**
     * Update an existing bundle
     */
    @Transactional
    @CacheEvict(value = {"bundles", "campsiteBundles"}, key = "#bundle.campsiteId")
    public Bundle updateBundle(Long bundleId, String name, String description, 
                             BigDecimal bundlePrice, List<BundleComponentRequest> components) {
        
        log.info("Updating bundle: {}", bundleId);
        
        Bundle bundle = getBundle(bundleId);
        
        // Update basic fields
        if (name != null && !name.trim().isEmpty()) {
            bundle.setName(name.trim());
        }
        
        if (description != null) {
            bundle.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        
        if (bundlePrice != null && bundlePrice.compareTo(BigDecimal.ZERO) >= 0) {
            bundle.setBundlePrice(bundlePrice);
        }
        
        // Update components if provided
        if (components != null) {
            validateBundleComponents(components);
            
            // Clear existing components
            bundle.getComponents().clear();
            
            // Add new components
            List<Long> productIds = components.stream()
                    .map(BundleComponentRequest::getProductId)
                    .collect(Collectors.toList());
            
            List<Product> products = productRepository.findActiveProductsByIds(productIds);
            
            if (products.size() != productIds.size()) {
                throw new ValidationException("Some products in the bundle are not found or inactive");
            }

            if (products.stream().anyMatch(product -> !product.isRentalItem())) {
                throw new ValidationException("Bundle components must be rental items");
            }
            
            Map<Long, Product> productMap = products.stream()
                    .collect(Collectors.toMap(Product::getId, product -> product));
            
            for (BundleComponentRequest componentReq : components) {
                Product product = productMap.get(componentReq.getProductId());
                bundle.addComponent(product, componentReq.getQuantity());
            }
        }
        
        // Validate updated bundle
        if (!bundle.isValid()) {
            throw new ValidationException("Bundle data is invalid after update");
        }
        validateBundleDiscount(bundle);
        
        Bundle updatedBundle = bundleRepository.save(bundle);
        log.info("Bundle updated: {}", bundleId);
        
        return updatedBundle;
    }

    /**
     * Delete a bundle (soft delete - archive)
     */
    @Transactional
    @CacheEvict(value = {"bundles", "campsiteBundles"}, key = "#bundle.campsiteId")
    public void deleteBundle(Long bundleId) {
        log.info("Archiving bundle: {}", bundleId);
        
        Bundle bundle = getBundle(bundleId);
        
        // Check if bundle is used in active bookings
        // TODO: Add checks when booking module is implemented
        
        bundle.setStatus(ProductStatus.ARCHIVED);
        bundleRepository.save(bundle);
        
        log.info("Bundle archived: {}", bundleId);
    }

    /**
     * Check bundle availability for given dates
     */
    public BundleAvailabilityResult checkBundleAvailability(Long bundleId, LocalDate checkIn, LocalDate checkOut) {
        log.debug("Checking availability for bundle: {} from {} to {}", bundleId, checkIn, checkOut);
        
        Bundle bundle = getBundle(bundleId);
        
        if (!bundle.isActive()) {
            return BundleAvailabilityResult.unavailable("Bundle is not active");
        }
        
        // Check availability of each component
        List<UnavailableComponent> unavailableComponents = bundle.getComponents().stream()
                .filter(component -> !isComponentAvailable(component.getProduct(), component.getQuantity(), checkIn, checkOut))
                .map(component -> UnavailableComponent.builder()
                        .productId(component.getProduct().getId())
                        .productName(component.getProduct().getName())
                        .reason(determineUnavailabilityReason(component.getProduct(), component.getQuantity()))
                        .build())
                .collect(Collectors.toList());
        
        if (unavailableComponents.isEmpty()) {
            return BundleAvailabilityResult.available();
        } else {
            return BundleAvailabilityResult.unavailable(unavailableComponents);
        }
    }

    /**
     * Search bundles by name or description
     */
    public Page<Bundle> searchBundles(String searchTerm, Integer page, Integer size, String sort) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ValidationException("Search term is required");
        }
        
        Pageable pageable = PageUtils.createPageable(page, size, sort);
        return bundleRepository.searchBundles(searchTerm.trim(), pageable);
    }

    /**
     * Validate bundle data
     */
    private void validateBundleData(String name, BigDecimal bundlePrice, List<BundleComponentRequest> components) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "Bundle name is required");
        }
        
        if (name.length() > 200) {
            throw new ValidationException("name", "Bundle name cannot exceed 200 characters");
        }
        
        if (bundlePrice == null || bundlePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("bundlePrice", "Bundle price must be non-negative");
        }
        
        validateBundleComponents(components);
    }

    /**
     * Validate bundle components
     */
    private void validateBundleComponents(List<BundleComponentRequest> components) {
        if (components == null || components.size() < 2) {
            throw new ValidationException("components", "Bundle must contain at least 2 components");
        }
        
        if (components.size() > 10) {
            throw new ValidationException("components", "Bundle cannot contain more than 10 components");
        }
        
        // Check for duplicate products
        List<Long> productIds = components.stream()
                .map(BundleComponentRequest::getProductId)
                .collect(Collectors.toList());
        
        if (productIds.size() != productIds.stream().distinct().count()) {
            throw new ValidationException("components", "Bundle cannot contain duplicate products");
        }
        
        // Validate each component
        for (BundleComponentRequest component : components) {
            if (component.getProductId() == null) {
                throw new ValidationException("components", "Product ID is required for all components");
            }
            if (component.getQuantity() == null || component.getQuantity() <= 0) {
                throw new ValidationException("components", "Quantity must be positive for all components");
            }
        }
    }

    private void validateBundleDiscount(Bundle bundle) {
        if (bundle.getBundlePrice() == null) {
            throw new ValidationException("bundlePrice", "Bundle price is required");
        }

        BigDecimal totalPrice = bundle.calculateIndividualPrice();
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }

        BigDecimal minimumPrice = totalPrice.multiply(BigDecimal.valueOf(0.5));
        if (bundle.getBundlePrice().compareTo(minimumPrice) < 0) {
            throw new ValidationException("bundlePrice", "Bundle discount cannot exceed 50%");
        }
    }

    /**
     * Check if a component is available
     */
    private boolean isComponentAvailable(Product product, int quantity, LocalDate checkIn, LocalDate checkOut) {
        if (!product.isActive()) {
            return false;
        }
        
        if (product.isRentalItem()) {
            // For rental products, check availability through booking system
            // TODO: Implement when booking module is ready
            return productStockService.getAvailableQuantity(product) >= quantity;
        }
        return false;
    }

    /**
     * Determine why a component is unavailable
     */
    private UnavailabilityReason determineUnavailabilityReason(Product product, int quantity) {
        if (!product.isActive()) {
            return UnavailabilityReason.PRODUCT_INACTIVE;
        }
        
        if (product.isRentalItem()) {
            int stockTotal = productStockService.getAvailableQuantity(product);
            if (stockTotal <= 0) {
                return UnavailabilityReason.OUT_OF_STOCK;
            }
            // TODO: Check booking conflicts when booking module is implemented
            return UnavailabilityReason.ALREADY_BOOKED;
        }
        return UnavailabilityReason.INVALID_COMPONENT;
    }

    // Helper classes for bundle operations
    public static class BundleComponentRequest {
        private Long productId;
        private Integer quantity;
        
        // Getters and setters
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class BundleAvailabilityResult {
        private boolean available;
        private String message;
        private List<UnavailableComponent> unavailableComponents;
        
        public static BundleAvailabilityResult available() {
            BundleAvailabilityResult result = new BundleAvailabilityResult();
            result.available = true;
            return result;
        }
        
        public static BundleAvailabilityResult unavailable(String message) {
            BundleAvailabilityResult result = new BundleAvailabilityResult();
            result.available = false;
            result.message = message;
            return result;
        }
        
        public static BundleAvailabilityResult unavailable(List<UnavailableComponent> components) {
            BundleAvailabilityResult result = new BundleAvailabilityResult();
            result.available = false;
            result.unavailableComponents = components;
            return result;
        }
        
        // Getters
        public boolean isAvailable() { return available; }
        public String getMessage() { return message; }
        public List<UnavailableComponent> getUnavailableComponents() { return unavailableComponents; }
    }

    @lombok.Builder
    @lombok.Data
    public static class UnavailableComponent {
        private Long productId;
        private String productName;
        private UnavailabilityReason reason;
    }

    public enum UnavailabilityReason {
        OUT_OF_STOCK,
        ALREADY_BOOKED,
        PRODUCT_INACTIVE,
        INVALID_COMPONENT,
        UNKNOWN
    }
}
