package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface KhoService {
    PhanHoi create(String name, String address, String manager);
    PhanHoi getAll();
    PhanHoi transfer(Long sanPhamId, Long fromWarehouseId, Long toWarehouseId, Integer quantity);
    PhanHoi thongTinTonKho(Long sanPhamId, Long khoId);
    PhanHoi thongKeTongTheoKho();
}
