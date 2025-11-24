package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "otp_reset")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OTPReset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String otp;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private boolean used;

    public boolean isExpired() {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }
}
