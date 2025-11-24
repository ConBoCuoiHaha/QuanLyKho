package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
public class SanPhamDTO {

    private Long id;
    private Long categoryId;
    private String name;
    private String sku;
    private BigDecimal price;

    private Integer stockQuantity;
    private Integer minStock;
    private Integer reservedQuantity;

    private String description;
    private LocalDateTime expiryDate;
    private String imageUrl;

    private LocalDateTime createdAt;
}
