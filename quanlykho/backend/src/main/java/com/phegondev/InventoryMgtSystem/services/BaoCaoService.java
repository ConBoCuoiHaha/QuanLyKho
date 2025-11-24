package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

import java.time.LocalDate;

public interface BaoCaoService {
    PhanHoi inventoryValuation();
    PhanHoi inventoryValuationByCategory();
    PhanHoi inventoryValuationByWarehouse();
    PhanHoi stockMovement(LocalDate from, LocalDate to, Long categoryId);
    PhanHoi agingInventory(int minDays);
    PhanHoi abcAnalysis(LocalDate from, LocalDate to);
    PhanHoi bestSellers(LocalDate from, LocalDate to, int limit, String metric);
    PhanHoi revenueProfit(LocalDate from, LocalDate to, String interval);
    PhanHoi supplierReport(LocalDate from, LocalDate to, int limit);
}
