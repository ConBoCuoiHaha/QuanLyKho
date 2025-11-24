package com.phegondev.InventoryMgtSystem.dtos;

import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonBanHang;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DonBanHangTrangThaiYeuCau {
    @NotNull(message = "Trang thai la bat buoc")
    private TrangThaiDonBanHang trangThai;
}
