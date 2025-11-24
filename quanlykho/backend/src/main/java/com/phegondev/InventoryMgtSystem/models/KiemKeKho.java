package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_take")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KiemKeKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Kho kho;

    private Integer soLuongHeThong;
    private Integer soLuongThucTe;
    private Integer chenhlech;

    private String lyDo;
    private String ghiChu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User nguoiDung;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @PrePersist
    public void prePersist() {
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }
}
