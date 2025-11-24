package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaoCaoXuatNhapTonDTO {
    private Long sanPhamId;
    private String tenSanPham;
    private String sku;
    private Long danhMucId;
    private String tenDanhMuc;
    private Integer tonDauKy;
    private Integer tongNhap;
    private Integer tongXuat;
    private Integer tonCuoiKy;
}
