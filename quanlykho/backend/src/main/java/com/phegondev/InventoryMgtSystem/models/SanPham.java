package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Data
@Builder
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên là bắt buộc")
    private String name;

    @Column(unique = true)
    @NotBlank(message = "SKU là bắt buộc")
    private String sku;

    @Positive(message = "Giá sản phẩm phải là số dương")
    private BigDecimal price;

    @Min(value = 0, message = "Số lượng tồn không được âm")
    private Integer stockQuantity;

    @Min(value = 0, message = "Mức tồn tối thiểu không được âm")
    @Column(name = "min_stock")
    private Integer minStock;

    @Min(value = 0, message = "So luong giu hang khong duoc am")
    @Column(name = "reserved_quantity")
    private Integer reservedQuantity;

    private String description;
    private LocalDateTime expiryDate;
    private String imageUrl;

    private final LocalDateTime createdAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private DanhMuc category;

    @PrePersist
    @PreUpdate
    private void ensureDefaults() {
        if (this.stockQuantity == null) {
            this.stockQuantity = 0;
        }
        if (this.minStock == null) {
            this.minStock = 0;
        }
        if (this.reservedQuantity == null) {
            this.reservedQuantity = 0;
        }
    }


    @Override
    public String toString() {
        return "SanPham{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", description='" + description + '\'' +
                ", expiryDate=" + expiryDate +
                ", imageUrl='" + imageUrl + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
