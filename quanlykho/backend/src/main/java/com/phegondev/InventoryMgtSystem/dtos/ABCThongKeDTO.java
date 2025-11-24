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
public class ABCThongKeDTO {
    private Integer tongSanPham;
    private Integer nhomA;
    private Integer nhomB;
    private Integer nhomC;
    private Double tyLeDoanhThuA;
    private Double tyLeDoanhThuB;
    private Double tyLeDoanhThuC;
}
