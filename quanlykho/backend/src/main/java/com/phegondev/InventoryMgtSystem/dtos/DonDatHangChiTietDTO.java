package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiChiTietDonDat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonDatHangChiTietDTO {
    private Long id;
    private SanPhamDTO sanPham;
    private Integer soLuong;
    private Integer soLuongDaNhan;
    private BigDecimal donGia;
    private TrangThaiChiTietDonDat trangThai;
}
