package com.electrasync.repository;

import com.electrasync.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT s FROM Sale s WHERE s.saleDate BETWEEN :from AND :to ORDER BY s.saleDate DESC")
    List<Sale> findSalesBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate BETWEEN :from AND :to AND s.status = 'COMPLETED'")
    BigDecimal sumRevenueBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    // Note: DATE() is a MySQL function - works because this project uses MySQL
    @Query("SELECT COUNT(s) FROM Sale s WHERE DATE(s.saleDate) = CURRENT_DATE")
    long countTodaySales();

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE DATE(s.saleDate) = CURRENT_DATE AND s.status = 'COMPLETED'")
    BigDecimal todayRevenue();
}
