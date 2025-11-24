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
public class DashboardTongQuanDTO {
    private BigDecimal doanhThuHomNay;
    private BigDecimal doanhThuThang;
    private BigDecimal tongGiaTriTon;
    private Long tongTonKho;
    private Long tongSanPham;
    private Long tongNhaCungCap;
    private Long sanPhamSapHet;
    private Long tongDonBan;
    private Long tongDonNhap;
}
