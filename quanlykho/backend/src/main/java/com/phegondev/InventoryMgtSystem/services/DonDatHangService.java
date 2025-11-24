package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.DonDatHangTrangThaiYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.DonDatHangYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.NhanHangYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface DonDatHangService {
    PhanHoi taoDon(DonDatHangYeuCau yeuCau);

    PhanHoi capNhatTrangThai(Long id, DonDatHangTrangThaiYeuCau yeuCau);

    PhanHoi danhSach(int page, int size, String trangThai, Long nhaCungCapId);

    PhanHoi chiTiet(Long id);

    PhanHoi nhanHang(Long id, NhanHangYeuCau yeuCau);
}
