package com.phegondev.InventoryMgtSystem.dtos.backup;

import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
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
public class GiaoDichBackupDTO {
    private LoaiGiaoDich transactionType;
    private TrangThaiGiaoDich status;
    private Integer totalProducts;
    private BigDecimal totalPrice;
    private String description;
    private String note;
    private LocalDateTime createdAt;
    private String sanPhamSku;
    private String userEmail;
    private String nhaCungCapEmail;
    private String khachHangEmail;
}
