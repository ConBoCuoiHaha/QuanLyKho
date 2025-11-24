package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonDatHang;
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
public class DonDatHangDTO {
    private Long id;
    private String maDon;
    private TrangThaiDonDatHang trangThai;
    private LocalDate ngayDuKien;
    private String ghiChu;
    private Integer tongSoLuong;
    private Integer tongSoLuongDaNhan;
    private BigDecimal tongTien;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private NhaCungCapDTO nhaCungCap;
    private UserDTO nguoiTao;
    private List<DonDatHangChiTietDTO> chiTiets;
}
