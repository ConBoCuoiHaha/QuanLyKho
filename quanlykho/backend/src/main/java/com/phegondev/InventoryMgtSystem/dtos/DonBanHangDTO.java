package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonBanHang;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonBanHangDTO {
    private Long id;
    private String maDon;
    private TrangThaiDonBanHang trangThai;
    private LocalDate ngayGiaoDuKien;
    private String ghiChu;
    private Integer tongSoLuong;
    private Integer tongSoLuongDaGiao;
    private BigDecimal tongTien;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private KhachHangDTO khachHang;
    private UserDTO nguoiTao;
    private KhoDTO kho;
    private List<DonBanHangChiTietDTO> chiTiets;
}
