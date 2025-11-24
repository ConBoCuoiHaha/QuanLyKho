package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface LoHangService {
    PhanHoi layLoSapHetHan(int days);
    PhanHoi layLoHetHan();
    PhanHoi huyLo(Long loHangId);
}
