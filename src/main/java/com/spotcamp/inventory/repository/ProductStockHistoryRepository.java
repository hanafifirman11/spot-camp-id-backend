package com.spotcamp.inventory.repository;

import com.spotcamp.inventory.domain.ProductStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockHistoryRepository extends JpaRepository<ProductStockHistory, Long> {
}
