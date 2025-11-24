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
public class TonKhoDTO {
    private Long sanPhamId;
    private String tenSanPham;
    private String sku;
    private Long khoId;
    private String tenKho;
    private Integer soLuong;
}
