package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouse_stock", uniqueConstraints = @UniqueConstraint(columnNames = {"product_id", "warehouse_id"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TonKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Kho warehouse;

    private Integer quantity;
}
