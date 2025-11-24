package com.phegondev.InventoryMgtSystem.dtos.backup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TonKhoBackupDTO {
    private String sanPhamSku;
    private String khoName;
    private Integer quantity;
}
