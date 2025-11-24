package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapDTO;

public interface NhaCungCapService {

    PhanHoi themNhaCungCap(NhaCungCapDTO nhaCungCapDTO);

    PhanHoi capNhatNhaCungCap(Long id, NhaCungCapDTO nhaCungCapDTO);

    PhanHoi layTatCaNhaCungCap();

    PhanHoi layNhaCungCapTheoId(Long id);

    PhanHoi xoaNhaCungCap(Long id);
}
