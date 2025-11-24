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
public class DashboardNhapXuatThangDTO {
    private Integer nam;
    private Integer thang;
    private String nhan;
    private BigDecimal tongNhap;
    private BigDecimal tongXuat;
}
