package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HangTonLauDTO {
    private Long loHangId;
    private Long sanPhamId;
    private String tenSanPham;
    private String sku;
    private Long danhMucId;
    private String tenDanhMuc;
    private String soLo;
    private LocalDate ngayNhap;
    private Integer soNgayTon;
    private Integer soLuongConLai;
    private String nhomTuoi;
}
