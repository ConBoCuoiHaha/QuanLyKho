package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.DonBanHangTrangThaiYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.DonBanHangYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface DonBanHangService {
    PhanHoi taoDon(DonBanHangYeuCau yeuCau);

    PhanHoi capNhatTrangThai(Long id, DonBanHangTrangThaiYeuCau yeuCau);

    PhanHoi danhSach(int page, int size, String trangThai, Long khachHangId);

    PhanHoi chiTiet(Long id);
}
