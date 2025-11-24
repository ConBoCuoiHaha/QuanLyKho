package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.NhatKyThayDoi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NhatKyThayDoiRepository extends JpaRepository<NhatKyThayDoi, Long>, JpaSpecificationExecutor<NhatKyThayDoi> {
}
