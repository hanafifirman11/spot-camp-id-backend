package com.spotcamp.module.inventory.controller;

import com.spotcamp.security.UserPrincipal;
import com.spotcamp.module.inventory.dto.*;
import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.entity.ProductStatus;
import com.spotcamp.module.inventory.entity.ProductType;
import com.spotcamp.module.inventory.service.ProductService;
import com.spotcamp.module.inventory.service.ProductStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Product Controller for campsite-scoped operations
 * Matches frontend API paths: /campsites/{campsiteId}/products
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product Management")
public class CampsiteProductController {

    private final ProductService productService;
    private final ProductStockService productStockService;

    @Operation(summary = "List products for a campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product list"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @GetMapping("/campsites/{campsiteId}/products")
    public ResponseEntity<ProductListResponseDTO> listCampsiteProducts(
            @PathVariable Long campsiteId,
            @RequestParam(required = false) ProductType type,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        log.debug("List products for campsite: {}", campsiteId);

        Page<Product> products = productService.getProducts(campsiteId, type, status, query, page, size, null);

        List<ProductResponseDTO> content = products.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ProductListResponseDTO.of(content, page, size, products.getTotalElements()));
    }

    @Operation(summary = "Create product for a campsite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Product created"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PostMapping("/campsites/{campsiteId}/products")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'MERCHANT_MEMBER', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ProductResponseDTO> createCampsiteProduct(
            @PathVariable Long campsiteId,
            @Valid @RequestBody ProductRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Create product for campsite: {} by user: {}", campsiteId, principal.getId());

        Product product = productService.createProduct(
                campsiteId,
                request.getName(),
                request.getDescription(),
                request.getType(),
                request.getCategory(),
                request.getItemType(),
                request.getPrice(),
                request.getStockTotal(),
                request.getBufferTimeMinutes(),
                request.getDailyRate(),
                request.getCurrentStock(),
                request.getReorderLevel(),
                request.getUnitPrice(),
                request.getImages()
        );

        return new ResponseEntity<>(mapToResponse(product), HttpStatus.CREATED);
    }

    @Operation(summary = "Get product details")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product details"),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/products/{productId}")
    public ResponseEntity<ProductResponseDTO> getProduct(@PathVariable Long productId) {
        log.debug("Get product: {}", productId);

        Product product = productService.getProduct(productId);
        return ResponseEntity.ok(mapToResponse(product));
    }

    @Operation(summary = "Update product")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Product updated"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PutMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long productId,
            @Valid @RequestBody ProductUpdateRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Update product: {} by user: {}", productId, principal.getId());

        Product product = productService.updateProduct(
                productId,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                request.getCategory(),
                request.getItemType(),
                request.getStockTotal(),
                request.getBufferTimeMinutes(),
                request.getDailyRate(),
                request.getCurrentStock(),
                request.getReorderLevel(),
                request.getUnitPrice(),
                request.getImages()
        );

        return ResponseEntity.ok(mapToResponse(product));
    }

    @Operation(summary = "Delete product (soft delete)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Product deleted"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @DeleteMapping("/products/{productId}")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable Long productId,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Delete product: {} by user: {}", productId, principal.getId());

        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update stock (rental/sale products)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Stock updated"),
        @ApiResponse(responseCode = "400", description = "Invalid operation (not a sale product)"),
        @ApiResponse(responseCode = "404", description = "Product not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    @PatchMapping("/products/{productId}/stock")
    @PreAuthorize("hasAnyRole('MERCHANT_ADMIN', 'SUPERADMIN', 'ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Map<String, Object>> updateStock(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateStockRequestDTO request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        log.info("Update stock for product: {} by user: {}", productId, principal.getId());

        Product product = productService.getProduct(productId);
        productStockService.adjustStock(product, request.getAdjustment(), request.getReason(), null, principal.getId());
        var stock = productStockService.findStock(productId).orElse(null);
        Integer currentStock = stock != null ? stock.getCurrentStock() : null;
        Integer stockTotal = stock != null ? stock.getStockTotal() : null;

        return ResponseEntity.ok(Map.of(
                "productId", product.getId(),
                "currentStock", currentStock,
                "stockTotal", stockTotal,
                "adjustment", request.getAdjustment()
        ));
    }

    private ProductResponseDTO mapToResponse(Product product) {
        ProductResponseDTO.ProductResponseDTOBuilder builder = ProductResponseDTO.builder()
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

        if (product.getImages() != null && !product.getImages().isEmpty()) {
            builder.images(product.getImages().stream()
                    .map(img -> img.getImageUrl())
                    .collect(Collectors.toList()));
        }

        var stock = productStockService.findStock(product.getId()).orElse(null);
        Integer stockTotal = stock != null ? stock.getStockTotal() : null;
        Integer currentStock = stock != null ? stock.getCurrentStock() : null;

        if (product.getType() == ProductType.RENTAL_SPOT || product.getType() == ProductType.RENTAL_ITEM) {
            builder.rentalDetails(ProductResponseDTO.RentalDetailsDTO.builder()
                    .stockTotal(stockTotal)
                    .bufferTime(product.getBufferTimeMinutes())
                    .dailyRate(product.getDailyRate())
                    .build());
        } else if (product.getType() == ProductType.SALE) {
            builder.saleDetails(ProductResponseDTO.SaleDetailsDTO.builder()
                    .currentStock(currentStock)
                    .reorderLevel(product.getReorderLevel())
                    .unitPrice(product.getUnitPrice())
                    .build());
        }

        return builder.build();
    }
}
