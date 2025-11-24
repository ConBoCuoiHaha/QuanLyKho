package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "nhat_ky_thay_doi")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NhatKyThayDoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String module;
    private String hanhDong;
    private String moTa;

    private String doiTuongLoai;
    private Long doiTuongId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String duLieuCu;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String duLieuMoi;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User nguoiThucHien;

    @PrePersist
    public void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
