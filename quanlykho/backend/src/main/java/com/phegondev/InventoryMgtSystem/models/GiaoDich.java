package com.phegondev.InventoryMgtSystem.models;

import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "giao_dich")
@Data
@Builder
public class GiaoDich {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer totalProducts;

    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    private LoaiGiaoDich transactionType; // pruchase, sale, return

    @Enumerated(EnumType.STRING)
    private TrangThaiGiaoDich status; //pending, completed, processing

    private String description;
    private String note;

    private final LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nha_cung_cap_id")
    private NhaCungCap nhaCungCap;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private KhachHang customer;

    @Override
    public String toString() {
        return "GiaoDich{" +
                "id=" + id +
                ", totalProducts=" + totalProducts +
                ", totalPrice=" + totalPrice +
                ", transactionType=" + transactionType +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updateAt=" + updateAt +
                '}';
    }
}
