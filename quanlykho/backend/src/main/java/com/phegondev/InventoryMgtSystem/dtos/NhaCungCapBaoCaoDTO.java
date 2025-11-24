package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class NhaCungCapBaoCaoDTO {
    private Long nhaCungCapId;
    private String tenNhaCungCap;
    private String soDienThoai;
    private String email;
    private Long soGiaoDich;
    private BigDecimal tongGiaTri;
    private Double tyLeDongGop;
    private Integer xepHang;
}
