package com.phegondev.InventoryMgtSystem.models;

import com.phegondev.InventoryMgtSystem.enums.TrangThaiChiTietDonDat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "don_dat_hang_chi_tiet")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonDatHangChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "don_dat_hang_id")
    private DonDatHang donDatHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "san_pham_id")
    private SanPham sanPham;

    private Integer soLuong;
    private Integer soLuongDaNhan;

    private BigDecimal donGia;

    @Enumerated(EnumType.STRING)
    private TrangThaiChiTietDonDat trangThai;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
