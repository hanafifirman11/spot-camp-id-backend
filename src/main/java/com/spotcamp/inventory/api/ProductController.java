package com.spotcamp.inventory.api;

import com.spotcamp.common.util.PageUtils;
import com.spotcamp.inventory.api.dto.ProductRentalRequest;
import com.spotcamp.inventory.api.dto.ProductResponse;
import com.spotcamp.inventory.api.dto.ProductSaleRequest;
import com.spotcamp.inventory.domain.Product;
import com.spotcamp.inventory.domain.ProductStatus;
import com.spotcamp.inventory.domain.ProductType;
import com.spotcamp.inventory.service.ProductService;
import com.spotcamp.inventory.service.ProductStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

/**
 * REST Controller for product management
 * Implements the inventory endpoints defined in the OpenAPI specification
 */
@Slf4j
@RestController
@RequestMapping("/inventory/products")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Product and bundle management")
public class ProductController {

    private final ProductService productService;
    private final ProductStockService productStockService;

    @Operation(
        summary = "List products",
        description = "Get paginated list of products with filters"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Products retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> listProducts(
            @Parameter(description = "Filter by campsite ID")
            @RequestParam(required = false) Long campsiteId,
            
            @Parameter(description = "Filter by product type")
            @RequestParam(required = false) ProductType type,
            
            @Parameter(description = "Filter by status")
            @RequestParam(required = false) ProductStatus status,

            @Parameter(description = "Search by name or description")
            @RequestParam(required = false) String query,
            
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") Integer page,
            
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") Integer size,
            
            @Parameter(description = "Sort criteria (e.g., 'name,asc' or 'createdAt,desc')")
            @RequestParam(required = false) String sort
    ) {
        log.debug("Getting products with filters - campsite: {}, type: {}, status: {}", 
                 campsiteId, type, status);
        
        Page<Product> products = productService.getProducts(campsiteId, type, status, query, page, size, sort);
        Page<ProductResponse> response = products.map(this::mapToProductResponse);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Create product",
        description = "Create a new product (rental or sale)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Product created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody Object request
    ) {
        log.info("Creating new product");
        
        Product product;
        
        if (request instanceof ProductRentalRequest rentalRequest) {
            product = productService.createProduct(
                    rentalRequest.getCampsiteId(),
                    rentalRequest.getName(),
                    rentalRequest.getDescription(),
                    rentalRequest.getType(),
                    rentalRequest.getCategory(),
                    rentalRequest.getItemType(),
                    rentalRequest.getBasePrice(),
                    rentalRequest.getStockTotal(),
                    rentalRequest.getBufferTime(),
                    rentalRequest.getDailyRate(),
                    null, null,
                    null,
                    rentalRequest.getImages()
            );
        } else if (request instanceof ProductSaleRequest saleRequest) {
            product = productService.createProduct(
                    saleRequest.getCampsiteId(),
                    saleRequest.getName(),
                    saleRequest.getDescription(),
                    saleRequest.getType(),
                    saleRequest.getCategory(),
                    saleRequest.getItemType(),
                    saleRequest.getBasePrice(),
                    null, null, null,
                    saleRequest.getCurrentStock(),
                    saleRequest.getReorderLevel(),
                    saleRequest.getUnitPrice(),
                    saleRequest.getImages()
            );
        } else {
            return ResponseEntity.badRequest().build();
        }
        
        ProductResponse response = mapToProductResponse(product);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Get product details",
        description = "Retrieve detailed information about a specific product"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product details retrieved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    @GetMapping("/{productId}")
    public ResponseEntity<ProductResponse> getProduct(
            @Parameter(description = "Unique identifier of the product")
            @PathVariable Long productId
    ) {
        log.debug("Getting product details for ID: {}", productId);
        
        Product product = productService.getProduct(productId);
        ProductResponse response = mapToProductResponse(product);
        
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Update product",
        description = "Update an existing product (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Product updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ProductResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ProductResponse> updateProduct(
            @Parameter(description = "Unique identifier of the product")
            @PathVariable Long productId,
            @Valid @RequestBody Object request
    ) {
        log.info("Updating product: {}", productId);
        
        Product product;
        
        if (request instanceof ProductRentalRequest rentalRequest) {
            product = productService.updateProduct(
                    productId,
                    rentalRequest.getName(),
                    rentalRequest.getDescription(),
                    rentalRequest.getBasePrice(),
                    rentalRequest.getCategory(),
                    rentalRequest.getItemType(),
                    rentalRequest.getStockTotal(),
                    rentalRequest.getBufferTime(),
                    rentalRequest.getDailyRate(),
                    null, null,
                    null,
                    rentalRequest.getImages()
            );
        } else if (request instanceof ProductSaleRequest saleRequest) {
            product = productService.updateProduct(
                    productId,
                    saleRequest.getName(),
                    saleRequest.getDescription(),
                    saleRequest.getBasePrice(),
                    saleRequest.getCategory(),
                    saleRequest.getItemType(),
                    null, null, null,
                    saleRequest.getCurrentStock(),
                    saleRequest.getReorderLevel(),
                    saleRequest.getUnitPrice(),
                    saleRequest.getImages()
            );
        } else {
            return ResponseEntity.badRequest().build();
        }
        
        ProductResponse response = mapToProductResponse(product);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Delete product (soft delete)",
        description = "Archive a product (Merchant/Admin only)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Product deleted successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Authentication required"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Product not found"
        )
    })
    @DeleteMapping("/{productId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Unique identifier of the product")
            @PathVariable Long productId
    ) {
        log.info("Deleting product: {}", productId);
        
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Map Product entity to ProductResponse DTO
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse.ProductResponseBuilder builder = ProductResponse.builder()
                .id(product.getId())
                .type(product.getType())
                .category(product.getCategory())
                .itemType(product.getItemType())
                .campsiteId(product.getCampsiteId())
                .name(product.getName())
                .description(product.getDescription())
                .basePrice(product.getBasePrice())
                .status(product.getStatus())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt());

        // Add images
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            builder.images(product.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList()));
        }

        // Add type-specific details
        var stock = productStockService.findStock(product.getId()).orElse(null);
        Integer stockTotal = stock != null ? stock.getStockTotal() : null;
        Integer currentStock = stock != null ? stock.getCurrentStock() : null;

        if (product.getType() == ProductType.RENTAL_SPOT || product.getType() == ProductType.RENTAL_ITEM) {
            builder.rentalDetails(ProductResponse.RentalDetailsDto.builder()
                    .stockTotal(stockTotal)
                    .bufferTime(product.getBufferTimeMinutes())
                    .dailyRate(product.getDailyRate())
                    .build());
        } else if (product.getType() == ProductType.SALE) {
            builder.saleDetails(ProductResponse.SaleDetailsDto.builder()
                    .currentStock(currentStock)
                    .reorderLevel(product.getReorderLevel())
                    .unitPrice(product.getUnitPrice())
                    .build());
        }

        return builder.build();
    }
}
