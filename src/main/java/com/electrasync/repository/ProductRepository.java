package com.electrasync.repository;

import com.electrasync.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // Used to check for duplicate SKU before saving
    Optional<Product> findBySku(String sku);

    List<Product> findByActiveTrue();

    // Products at or below their minimum stock threshold — shown in low-stock alerts
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= p.minimumStock AND p.active = true")
    List<Product> findLowStockProducts();

    // Used by the POS product search box
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) AND p.active = true")
    List<Product> searchByName(@org.springframework.data.repository.query.Param("keyword") String keyword);
}
