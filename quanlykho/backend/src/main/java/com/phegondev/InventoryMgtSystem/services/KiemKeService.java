package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauKiemKe;

public interface KiemKeService {
    PhanHoi thucHienKiemKe(YeuCauKiemKe yeuCau);

    PhanHoi layLichSuKiemKe(Long sanPhamId, Long khoId, int page, int size);

    PhanHoi hienTrangTonKho(Long sanPhamId, Long khoId);
}
