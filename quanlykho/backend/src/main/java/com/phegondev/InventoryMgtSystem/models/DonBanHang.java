package com.phegondev.InventoryMgtSystem.models;

import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonBanHang;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "don_ban_hang")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DonBanHang {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_don", unique = true, nullable = false, length = 40)
    private String maDon;

    @Enumerated(EnumType.STRING)
    private TrangThaiDonBanHang trangThai;

    private LocalDate ngayGiaoDuKien;

    @Column(length = 1000)
    private String ghiChu;

    private Integer tongSoLuong;
    private Integer tongSoLuongDaGiao;
    private BigDecimal tongTien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kho_id")
    private Kho kho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id")
    private User nguoiTao;

    @OneToMany(mappedBy = "donBanHang", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DonBanHangChiTiet> chiTiets = new ArrayList<>();

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
