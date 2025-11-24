package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "batches")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private SanPham sanPham;

    @Column(name = "lot_number")
    private String lotNumber;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "quantity_remaining")
    private Integer quantityRemaining;
}
