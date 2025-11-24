package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SanPhamRepository extends JpaRepository<SanPham, Long> {
    List<SanPham> findByNameContainingOrDescriptionContaining(String name, String description);

    @Query("SELECT p FROM SanPham p WHERE p.minStock IS NOT NULL AND p.stockQuantity <= p.minStock")
    List<SanPham> findLowStockProducts();

    @Query("SELECT p FROM SanPham p WHERE p.expiryDate IS NOT NULL AND p.expiryDate BETWEEN :from AND :to")
    List<SanPham> findExpiringBetween(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT p FROM SanPham p WHERE p.expiryDate IS NOT NULL AND p.expiryDate < :now")
    List<SanPham> findExpired(@Param("now") LocalDateTime now);

    Optional<SanPham> findBySkuIgnoreCase(String sku);

    @Query("""
            SELECT p FROM SanPham p
            WHERE (:keyword IS NULL OR lower(p.name) LIKE lower(concat('%', :keyword, '%'))
                   OR lower(p.sku) LIKE lower(concat('%', :keyword, '%')))
              AND (:categoryId IS NULL OR p.category.id = :categoryId)
            """)
    Page<SanPham> search(@Param("keyword") String keyword,
                         @Param("categoryId") Long categoryId,
                         Pageable pageable);

    List<SanPham> findByCategory_Id(Long categoryId);

    @Query(value = "SELECT COALESCE(SUM(stock_quantity), 0) FROM products", nativeQuery = true)
    Long sumTotalStock();

    @Query(value = "SELECT COALESCE(SUM(stock_quantity * price), 0) FROM products", nativeQuery = true)
    BigDecimal sumTotalStockValue();
}
