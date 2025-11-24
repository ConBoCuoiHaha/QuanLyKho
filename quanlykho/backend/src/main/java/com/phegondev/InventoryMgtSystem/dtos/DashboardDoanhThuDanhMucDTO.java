package com.phegondev.InventoryMgtSystem.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDoanhThuDanhMucDTO {
    private Long danhMucId;
    private String tenDanhMuc;
    private BigDecimal doanhThu;
    private Double tyLeDongGop;
}
