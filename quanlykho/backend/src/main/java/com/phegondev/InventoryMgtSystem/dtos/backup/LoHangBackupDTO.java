package com.phegondev.InventoryMgtSystem.dtos.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoHangBackupDTO {
    private String sanPhamSku;
    private String lotNumber;
    private LocalDate receivedDate;
    private LocalDate expiryDate;
    private Integer quantityRemaining;
}
