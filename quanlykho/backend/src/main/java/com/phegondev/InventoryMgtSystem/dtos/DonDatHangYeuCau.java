package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DonDatHangYeuCau {

    @NotNull(message = "Nha cung cap la bat buoc")
    private Long nhaCungCapId;

    private LocalDate ngayDuKien;
    private String ghiChu;

    @NotEmpty(message = "Danh sach san pham la bat buoc")
    @Valid
    private List<ItemYeuCau> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemYeuCau {
        @NotNull(message = "San pham la bat buoc")
        private Long sanPhamId;

        @Positive(message = "So luong phai lon hon 0")
        private Integer soLuong;

        @Positive(message = "Don gia phai lon hon 0")
        private BigDecimal donGia;
    }
}
