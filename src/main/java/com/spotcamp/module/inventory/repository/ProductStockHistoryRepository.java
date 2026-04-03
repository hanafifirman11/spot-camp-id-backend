package com.spotcamp.module.inventory.repository;

import com.spotcamp.module.inventory.entity.ProductStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductStockHistoryRepository extends JpaRepository<ProductStockHistory, Long> {
}
