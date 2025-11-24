package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.KhachHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface KhachHangService {
    PhanHoi add(KhachHangDTO dto);
    PhanHoi update(Long id, KhachHangDTO dto);
    PhanHoi getAll();
    PhanHoi getById(Long id);
    PhanHoi detail(Long id);
    PhanHoi delete(Long id);
}
