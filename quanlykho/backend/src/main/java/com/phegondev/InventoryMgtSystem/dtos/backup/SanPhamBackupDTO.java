package com.phegondev.InventoryMgtSystem.dtos.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SanPhamBackupDTO {
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockQuantity;
    private Integer minStock;
    private Integer reservedQuantity;
    private String description;
    private LocalDateTime expiryDate;
    private String imageUrl;
    private String categoryName;
}
