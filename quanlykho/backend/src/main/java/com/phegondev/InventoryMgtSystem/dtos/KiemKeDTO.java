package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KiemKeDTO {
    private Long id;
    private SanPhamDTO sanPham;
    private Long khoId;
    private String tenKho;
    private Integer soLuongHeThong;
    private Integer soLuongThucTe;
    private Integer chenhlech;
    private String lyDo;
    private String ghiChu;
    private String nguoiThucHien;
    private LocalDateTime thoiGian;
}
