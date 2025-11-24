package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.KiemKeKho;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KiemKeRepository extends JpaRepository<KiemKeKho, Long> {
    Page<KiemKeKho> findBySanPhamId(Long sanPhamId, Pageable pageable);

    Page<KiemKeKho> findByKhoId(Long khoId, Pageable pageable);

    Page<KiemKeKho> findBySanPhamIdAndKhoId(Long sanPhamId, Long khoId, Pageable pageable);
}
