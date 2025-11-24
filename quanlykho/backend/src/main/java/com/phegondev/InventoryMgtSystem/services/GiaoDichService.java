package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;

public interface GiaoDichService {
    PhanHoi nhapKho(YeuCauGiaoDich yeuCau);

    PhanHoi banHang(YeuCauGiaoDich yeuCau);

    PhanHoi traVeNhaCungCap(YeuCauGiaoDich yeuCau);

    PhanHoi khachHangTra(YeuCauGiaoDich yeuCau);

    PhanHoi chuyenKho(YeuCauGiaoDich yeuCau);

    PhanHoi layTatCaGiaoDich(int page, int size, String filter);

    PhanHoi layGiaoDichTheoId(Long id);

    PhanHoi layGiaoDichTheoThangNam(int month, int year);

    PhanHoi capNhatTrangThai(Long giaoDichId, TrangThaiGiaoDich status);
}
