package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiChiTietDonBan;
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
public class DonBanHangChiTietDTO {
    private Long id;
    private SanPhamDTO sanPham;
    private Integer soLuong;
    private Integer soLuongDaGiao;
    private BigDecimal donGia;
    private TrangThaiChiTietDonBan trangThai;
}
