package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.LoHang;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoHangRepository extends JpaRepository<LoHang, Long> {
    List<LoHang> findBySanPhamOrderByExpiryDateAsc(SanPham sanPham);

    @Query("SELECT l FROM LoHang l WHERE l.expiryDate IS NOT NULL AND l.expiryDate BETWEEN :from AND :to")
    List<LoHang> findExpiringBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT l FROM LoHang l WHERE l.expiryDate IS NOT NULL AND l.expiryDate < :date")
    List<LoHang> findExpired(@Param("date") LocalDate date);

    Optional<LoHang> findBySanPhamAndLotNumber(SanPham sanPham, String lotNumber);

    List<LoHang> findByQuantityRemainingGreaterThan(Integer quantity);
}
