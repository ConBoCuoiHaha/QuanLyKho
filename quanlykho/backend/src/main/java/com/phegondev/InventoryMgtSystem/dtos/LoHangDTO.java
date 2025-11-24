package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoHangDTO {
    private Long id;
    private String lotNumber;
    private LocalDate receivedDate;
    private LocalDate expiryDate;
    private Integer quantityRemaining;
    private SanPhamDTO sanPham;
}
