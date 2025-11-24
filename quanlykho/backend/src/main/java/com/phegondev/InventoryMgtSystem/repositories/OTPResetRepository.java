package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.OTPReset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OTPResetRepository extends JpaRepository<OTPReset, Long> {
    Optional<OTPReset> findByEmail(String email);
}
