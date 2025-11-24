package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class GiaoDichDTO {

    private Long id;

    private Integer totalProducts;

    private BigDecimal totalPrice;


    private LoaiGiaoDich transactionType; // pruchase, sale, return


    private TrangThaiGiaoDich status; //pending, completed, processing

    private String description;
    private String note;

    private LocalDateTime createdAt;
    private LocalDateTime updateAt;

    private SanPhamDTO sanPham;

    private UserDTO user;

    private NhaCungCapDTO nhaCungCap;


}
