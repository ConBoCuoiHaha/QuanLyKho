package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface DashboardService {
    PhanHoi tongQuan(Integer year, Integer month, Integer lowStockLimit);
}
