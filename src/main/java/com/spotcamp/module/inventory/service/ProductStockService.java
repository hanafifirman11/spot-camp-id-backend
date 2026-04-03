package com.spotcamp.module.inventory.service;

import com.spotcamp.common.exception.BusinessException;
import com.spotcamp.module.inventory.entity.Product;
import com.spotcamp.module.inventory.entity.ProductStock;
import com.spotcamp.module.inventory.entity.ProductStockHistory;
import com.spotcamp.module.inventory.entity.StockChangeType;
import com.spotcamp.module.inventory.repository.ProductStockHistoryRepository;
import com.spotcamp.module.inventory.repository.ProductStockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductStockService {

    private final ProductStockRepository productStockRepository;
    private final ProductStockHistoryRepository productStockHistoryRepository;

    public Optional<ProductStock> findStock(Long productId) {
        return productStockRepository.findByProductId(productId);
    }

    @Transactional
    public ProductStock getOrCreateStock(Product product, Integer stockTotal, Integer currentStock, Long actorId, String remark) {
        return productStockRepository.findByProductIdForUpdate(product.getId())
                .orElseGet(() -> {
                    ProductStock created = ProductStock.builder()
                            .productId(product.getId())
                            .stockTotal(stockTotal)
                            .currentStock(currentStock)
                            .updatedAt(LocalDateTime.now())
                            .updatedBy(actorId)
                            .build();
                    ProductStock saved = productStockRepository.save(created);
                    recordHistory(product.getId(), null, StockChangeType.ADJUST, 0, getBalanceForProduct(product, saved), remark, actorId);
                    return saved;
                });
    }

    @Transactional
    public ProductStock adjustStock(Product product, int adjustment, String remark, Long bookingId, Long actorId) {
        ProductStock stock = productStockRepository.findByProductIdForUpdate(product.getId())
                .orElseGet(() -> ProductStock.builder()
                        .productId(product.getId())
                        .stockTotal(null)
                        .currentStock(null)
                        .updatedAt(LocalDateTime.now())
                        .updatedBy(actorId)
                        .build());

        if (product.isRentalItem() || product.isRentalSpot()) {
            int current = stock.getStockTotal() == null ? 0 : stock.getStockTotal();
            int next = current + adjustment;
            if (next < 0) {
                throw new BusinessException("Stock total cannot be negative");
            }
            stock.setStockTotal(next);
            stock.setUpdatedAt(LocalDateTime.now());
            stock.setUpdatedBy(actorId);
            ProductStock saved = productStockRepository.save(stock);
            recordHistory(product.getId(), bookingId, adjustment >= 0 ? StockChangeType.IN : StockChangeType.OUT, Math.abs(adjustment), next, remark, actorId);
            return saved;
        }

        if (product.isSale()) {
            int current = stock.getCurrentStock() == null ? 0 : stock.getCurrentStock();
            int next = current + adjustment;
            if (next < 0) {
                throw new BusinessException("Insufficient stock available");
            }
            stock.setCurrentStock(next);
            stock.setUpdatedAt(LocalDateTime.now());
            stock.setUpdatedBy(actorId);
            ProductStock saved = productStockRepository.save(stock);
            recordHistory(product.getId(), bookingId, adjustment >= 0 ? StockChangeType.IN : StockChangeType.OUT, Math.abs(adjustment), next, remark, actorId);
            return saved;
        }

        return stock;
    }

    public int getAvailableQuantity(Product product) {
        ProductStock stock = productStockRepository.findByProductId(product.getId()).orElse(null);
        if (product.isRentalItem() || product.isRentalSpot()) {
            return stock != null && stock.getStockTotal() != null ? stock.getStockTotal() : 0;
        }
        if (product.isSale()) {
            return stock != null && stock.getCurrentStock() != null ? stock.getCurrentStock() : 0;
        }
        return 0;
    }

    private void recordHistory(Long productId, Long bookingId, StockChangeType type, int quantity,
                               Integer balanceAfter, String remark, Long actorId) {
        ProductStockHistory history = ProductStockHistory.builder()
                .productId(productId)
                .bookingId(bookingId)
                .changeType(type)
                .quantity(quantity)
                .balanceAfter(balanceAfter)
                .remark(remark)
                .createdAt(LocalDateTime.now())
                .createdBy(actorId)
                .build();
        productStockHistoryRepository.save(history);
    }

    private Integer getBalanceForProduct(Product product, ProductStock stock) {
        if (product.isSale()) {
            return stock.getCurrentStock();
        }
        return stock.getStockTotal();
    }
}
