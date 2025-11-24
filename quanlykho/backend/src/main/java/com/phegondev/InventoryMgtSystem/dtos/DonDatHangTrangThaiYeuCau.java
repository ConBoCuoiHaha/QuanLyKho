package com.phegondev.InventoryMgtSystem.dtos;

import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonDatHang;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DonDatHangTrangThaiYeuCau {
    @NotNull(message = "Trang thai la bat buoc")
    private TrangThaiDonDatHang trangThai;
}
