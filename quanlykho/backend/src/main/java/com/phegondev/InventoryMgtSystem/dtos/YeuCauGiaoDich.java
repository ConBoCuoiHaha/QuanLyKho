package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class YeuCauGiaoDich {

    @Positive(message = "Ma san pham la bat buoc")
    private Long sanPhamId;

    @Positive(message = "So luong la bat buoc")
    private Integer soLuong;

    private Long nhaCungCapId;

    private Long khachHangId;

    private Long khoId;
    private Long khoDichId;

    private String moTa;

    private String ghiChu;

    private String soLo;
    private LocalDate ngayNhap;
    private LocalDate hanSuDung;
}
