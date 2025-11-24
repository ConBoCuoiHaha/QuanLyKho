package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.Kho;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.models.TonKho;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TonKhoRepository extends JpaRepository<TonKho, Long> {
    Optional<TonKho> findBySanPhamAndWarehouse(SanPham sanPham, Kho warehouse);

    List<TonKho> findBySanPham(SanPham sanPham);

    List<TonKho> findByWarehouse(Kho warehouse);
}
