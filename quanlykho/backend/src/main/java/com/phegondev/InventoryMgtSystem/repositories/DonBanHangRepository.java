package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.DonBanHang;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DonBanHangRepository extends JpaRepository<DonBanHang, Long>, JpaSpecificationExecutor<DonBanHang> {

    @EntityGraph(attributePaths = {"chiTiets", "chiTiets.sanPham", "khachHang", "kho", "nguoiTao"})
    Optional<DonBanHang> findWithChiTietsById(Long id);
}
