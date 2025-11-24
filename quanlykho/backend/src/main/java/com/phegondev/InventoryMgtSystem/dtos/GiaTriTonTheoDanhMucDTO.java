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
public class GiaTriTonTheoDanhMucDTO {
    private Long danhMucId;
    private String tenDanhMuc;
    private Integer tongSoLuong;
    private BigDecimal giaTriTon;
}
