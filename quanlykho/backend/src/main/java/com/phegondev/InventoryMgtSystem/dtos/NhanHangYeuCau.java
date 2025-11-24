package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NhanHangYeuCau {

    private String ghiChu;

    @NotEmpty(message = "Chi tiet nhan hang la bat buoc")
    @Valid
    private List<ChiTietNhanYeuCau> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChiTietNhanYeuCau {
        @NotNull(message = "Chi tiet don la bat buoc")
        private Long chiTietId;

        @Positive(message = "So luong nhan phai lon hon 0")
        private Integer soLuongNhan;

        private Long khoId;
        private String soLo;
        private LocalDate ngayNhap;
        private LocalDate hanSuDung;
    }
}
