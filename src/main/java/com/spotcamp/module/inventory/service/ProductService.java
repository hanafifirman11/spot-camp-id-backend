package com.spotcamp.module.inventory.service;

import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.common.exception.ResourceNotFoundException;
import com.spotcamp.common.exception.ValidationException;
import com.spotcamp.common.util.PageUtils;
import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.entity.ProductCategory;
import com.spotcamp.module.inventory.entity.ProductItemType;
import com.spotcamp.module.inventory.entity.ProductStatus;
import com.spotcamp.module.inventory.entity.ProductType;
import com.spotcamp.module.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service for product management operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductStockService productStockService;

    /**
     * Get products with filters and pagination
     */
    @Cacheable(value = "products", key = "#campsiteId + ':' + #type + ':' + #status + ':' + #query + ':' + #page + ':' + #size")
    @Transactional(readOnly = true)
    public Page<Product> getProducts(Long campsiteId, ProductType type, ProductStatus status, String query,
                                    Integer page, Integer size, String sort) {
        
        log.debug("Getting products with filters - campsite: {}, type: {}, status: {}", 
                 campsiteId, type, status);
        
        Pageable pageable = PageUtils.createPageable(page, size, sort);
        String trimmedQuery = (query == null || query.trim().isEmpty()) ? null : query.trim();
        Page<Product> products = productRepository.findByFilters(campsiteId, type, status, trimmedQuery, pageable);
        products.forEach(product -> Hibernate.initialize(product.getImages()));
        return products;
    }

    /**
     * Get product by ID
     */
    @Transactional(readOnly = true)
    public Product getProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        Hibernate.initialize(product.getImages());
        return product;
    }

    /**
     * Find product by ID without throwing (for optional flows).
     */
    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }

    /**
     * Get product by ID with pessimistic write lock.
     */
    @Transactional
    public Product getProductForUpdate(Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
    }

    /**
     * Get active products by campsite
     */
    @Cacheable(value = "campsiteProducts", key = "#campsiteId")
    public List<Product> getActiveProductsByCampsite(Long campsiteId) {
        return productRepository.findByCampsiteIdAndStatusOrderByNameAsc(campsiteId, ProductStatus.ACTIVE);
    }

    /**
     * Create a new product
     */
    @Transactional
    @CacheEvict(value = {"products", "campsiteProducts"}, allEntries = true)
    public Product createProduct(Long campsiteId, String name, String description, ProductType type,
                               ProductCategory category, ProductItemType itemType, BigDecimal basePrice,
                               Integer stockTotal, Integer bufferTimeMinutes, BigDecimal dailyRate,
                               Integer currentStock, Integer reorderLevel, BigDecimal unitPrice,
                               List<String> imageUrls) {
        
        log.info("Creating new product: {} for campsite: {}", name, campsiteId);
        
        // Validate input
        ProductCategory resolvedCategory = resolveCategory(type, category);
        ProductItemType resolvedItemType = resolveItemType(type, itemType);
        validateProductData(name, type, basePrice, stockTotal, dailyRate, currentStock, unitPrice);
        
        Product.ProductBuilder builder = Product.builder()
                .campsiteId(campsiteId)
                .name(name.trim())
                .description(description != null ? description.trim() : null)
                .type(type)
                .category(resolvedCategory)
                .itemType(resolvedItemType)
                .basePrice(basePrice)
                .status(ProductStatus.ACTIVE);

        // Set type-specific fields
        if (type == ProductType.RENTAL_SPOT || type == ProductType.RENTAL_ITEM) {
            builder.stockTotal(stockTotal)
                   .bufferTimeMinutes(bufferTimeMinutes != null ? bufferTimeMinutes : 120)
                   .dailyRate(dailyRate);
        } else if (type == ProductType.SALE) {
            builder.currentStock(currentStock != null ? currentStock : 0)
                   .reorderLevel(reorderLevel != null ? reorderLevel : 10)
                   .unitPrice(unitPrice);
        }

        Product product = builder.build();
        
        // Validate product consistency
        if (!product.isValid()) {
            throw new ValidationException("Product data is incomplete for the specified type");
        }
        
        Product savedProduct = productRepository.save(product);

        productStockService.getOrCreateStock(savedProduct, stockTotal, currentStock, null, "Initial stock");
        
        // Add images if provided
        if (imageUrls != null && !imageUrls.isEmpty()) {
            addProductImages(savedProduct, imageUrls);
            savedProduct = productRepository.save(savedProduct);
        }
        
        log.info("Product created with ID: {}", savedProduct.getId());
        return savedProduct;
    }

    /**
     * Update an existing product
     */
    @Transactional
    @CacheEvict(value = {"products", "campsiteProducts"}, allEntries = true)
    public Product updateProduct(Long productId, String name, String description, BigDecimal basePrice,
                               ProductCategory category, ProductItemType itemType, Integer stockTotal,
                               Integer bufferTimeMinutes, BigDecimal dailyRate, Integer currentStock,
                               Integer reorderLevel, BigDecimal unitPrice, List<String> imageUrls) {
        
        log.info("Updating product: {}", productId);
        
        Product product = getProduct(productId);

        ProductCategory resolvedCategory = category != null
                ? resolveCategory(product.getType(), category)
                : (product.getCategory() != null ? product.getCategory() : resolveCategory(product.getType(), null));
        ProductItemType resolvedItemType = itemType != null
                ? resolveItemType(product.getType(), itemType)
                : (product.getItemType() != null ? product.getItemType() : resolveItemType(product.getType(), null));
        product.setCategory(resolvedCategory);
        product.setItemType(resolvedItemType);
        
        // Update basic fields
        if (name != null && !name.trim().isEmpty()) {
            product.setName(name.trim());
        }
        
        if (description != null) {
            product.setDescription(description.trim().isEmpty() ? null : description.trim());
        }
        
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) >= 0) {
            product.setBasePrice(basePrice);
        }
        
        // Update type-specific fields
        if (product.getType() == ProductType.RENTAL_SPOT || product.getType() == ProductType.RENTAL_ITEM) {
            if (bufferTimeMinutes != null && bufferTimeMinutes >= 0) {
                product.setBufferTimeMinutes(bufferTimeMinutes);
            }
            if (dailyRate != null && dailyRate.compareTo(BigDecimal.ZERO) > 0) {
                product.setDailyRate(dailyRate);
            }
        } else if (product.getType() == ProductType.SALE) {
            if (reorderLevel != null && reorderLevel >= 0) {
                product.setReorderLevel(reorderLevel);
            }
            if (unitPrice != null && unitPrice.compareTo(BigDecimal.ZERO) > 0) {
                product.setUnitPrice(unitPrice);
            }
        }
        
        // Update images if provided
        if (imageUrls != null) {
            product.getImages().clear();
            if (!imageUrls.isEmpty()) {
                addProductImages(product, imageUrls);
            }
        }
        
        // Validate updated product
        if (!product.isValid()) {
            throw new ValidationException("Product data is incomplete after update");
        }
        
        Product updatedProduct = productRepository.save(product);
        log.info("Product updated: {}", productId);
        
        return updatedProduct;
    }

    /**
     * Delete a product (soft delete - archive)
     */
    @Transactional
    @CacheEvict(value = {"products", "campsiteProducts"}, allEntries = true)
    public void deleteProduct(Long productId) {
        log.info("Archiving product: {}", productId);
        
        Product product = getProduct(productId);
        
        // Check if product is used in active bookings or bundles
        // TODO: Add checks when booking and bundle modules are implemented
        
        product.setStatus(ProductStatus.ARCHIVED);
        productRepository.save(product);
        
        log.info("Product archived: {}", productId);
    }

    /**
     * Reduce stock for sale products (used during checkout)
     */
    @Transactional
    public void reduceStock(Long productId, int quantity) {
        log.debug("Reducing stock for product: {} by quantity: {}", productId, quantity);
        
        Product product = getProduct(productId);
        
        if (product.getType() != ProductType.SALE) {
            throw new BusinessException("Cannot reduce stock for rental products");
        }

        productStockService.adjustStock(product, -quantity, "Sale stock reduction", null, null);
        log.debug("Stock reduced successfully for product: {}", productId);
    }

    /**
     * Increase stock for sale products (used for restocking)
     */
    @Transactional
    public void increaseStock(Long productId, int quantity) {
        log.debug("Increasing stock for product: {} by quantity: {}", productId, quantity);

        Product product = getProduct(productId);

        if (product.getType() != ProductType.SALE) {
            throw new BusinessException("Cannot increase stock for rental products");
        }

        productStockService.adjustStock(product, quantity, "Sale stock increase", null, null);
        log.debug("Stock increased successfully for product: {}", productId);
    }

    /**
     * Adjust stock for sale products (positive = increase, negative = decrease)
     */
    @Transactional
    public Product adjustStock(Long productId, int adjustment, String reason) {
        log.info("Adjusting stock for product: {} by: {} (reason: {})", productId, adjustment, reason);

        Product product = getProduct(productId);

        if (product.getType() != ProductType.SALE) {
            throw new BusinessException("Stock adjustment is only for sale products");
        }

        productStockService.adjustStock(product, adjustment, reason, null, null);
        return getProduct(productId);
    }

    /**
     * Get products that need reordering
     */
    public List<Product> getProductsNeedingReorder(Long campsiteId) {
        if (campsiteId != null) {
            return productRepository.findProductsNeedingReorder(campsiteId);
        } else {
            return productRepository.findAllProductsNeedingReorder();
        }
    }

    /**
     * Search products by name or description
     */
    public Page<Product> searchProducts(String searchTerm, Integer page, Integer size, String sort) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new ValidationException("Search term is required");
        }
        
        Pageable pageable = PageUtils.createPageable(page, size, sort);
        return productRepository.searchProducts(searchTerm.trim(), pageable);
    }

    /**
     * Validate product data
     */
    private void validateProductData(String name, ProductType type, BigDecimal basePrice,
                                   Integer stockTotal, BigDecimal dailyRate,
                                   Integer currentStock, BigDecimal unitPrice) {
        
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("name", "Product name is required");
        }
        
        if (name.length() > 200) {
            throw new ValidationException("name", "Product name cannot exceed 200 characters");
        }
        
        if (type == null) {
            throw new ValidationException("type", "Product type is required");
        }
        
        if (basePrice == null || basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new ValidationException("basePrice", "Base price must be non-negative");
        }
        
        // Type-specific validation
        if (type == ProductType.RENTAL_SPOT) {
            if (stockTotal == null || stockTotal <= 0) {
                throw new ValidationException("stockTotal", "Stock total is required for rental map products");
            }
            if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("dailyRate", "Daily rate is required for rental products");
            }
        } else if (type == ProductType.RENTAL_ITEM) {
            if (stockTotal == null || stockTotal < 0) {
                throw new ValidationException("stockTotal", "Stock total cannot be negative for rental items");
            }
            if (dailyRate == null || dailyRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("dailyRate", "Daily rate is required for rental items");
            }
        } else if (type == ProductType.SALE) {
            if (currentStock != null && currentStock < 0) {
                throw new ValidationException("currentStock", "Current stock cannot be negative");
            }
            if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("unitPrice", "Unit price is required for sale products");
            }
        }
    }

    private ProductCategory resolveCategory(ProductType type, ProductCategory category) {
        ProductCategory inferred = switch (type) {
            case RENTAL_SPOT, RENTAL_ITEM -> ProductCategory.RENTAL;
            case SALE -> ProductCategory.SALE;
        };
        if (category == null) {
            return inferred;
        }
        if (category != inferred) {
            throw new ValidationException("category", "Category must match the product type");
        }
        return category;
    }

    private ProductItemType resolveItemType(ProductType type, ProductItemType itemType) {
        ProductItemType fallback = switch (type) {
            case RENTAL_SPOT -> ProductItemType.SPOT;
            case RENTAL_ITEM -> ProductItemType.EQUIPMENT;
            case SALE -> ProductItemType.GOODS;
        };
        if (itemType == null) {
            return fallback;
        }
        boolean valid = switch (type) {
            case RENTAL_SPOT -> itemType == ProductItemType.SPOT;
            case RENTAL_ITEM -> itemType == ProductItemType.TENT || itemType == ProductItemType.EQUIPMENT;
            case SALE -> itemType == ProductItemType.GOODS || itemType == ProductItemType.FNB;
        };
        if (!valid) {
            throw new ValidationException("itemType", "Item type is not valid for the product type");
        }
        return itemType;
    }

    /**
     * Add images to product
     */
    private void addProductImages(Product product, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size() && i < 5; i++) { // Max 5 images
            String imageUrl = imageUrls.get(i);
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                product.addImage(imageUrl.trim(), i);
            }
        }
    }
}
