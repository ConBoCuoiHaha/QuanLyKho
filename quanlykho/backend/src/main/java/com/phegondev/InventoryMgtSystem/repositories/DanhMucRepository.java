package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.DanhMuc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DanhMucRepository extends JpaRepository<DanhMuc, Long> {
    Optional<DanhMuc> findByNameIgnoreCase(String name);
}
