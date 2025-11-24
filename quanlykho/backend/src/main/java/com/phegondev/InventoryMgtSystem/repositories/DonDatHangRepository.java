package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.DonDatHang;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DonDatHangRepository extends JpaRepository<DonDatHang, Long>, JpaSpecificationExecutor<DonDatHang> {

    @EntityGraph(attributePaths = {"chiTiets", "chiTiets.sanPham", "nhaCungCap", "nguoiTao"})
    Optional<DonDatHang> findWithChiTietsById(Long id);
}
