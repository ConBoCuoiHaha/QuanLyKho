package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class KhachHangDTO {
    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String notes;
    private Long tongGiaoDich;
    private BigDecimal tongChiTieu;
    private BigDecimal congNo;
}
