package com.phegondev.InventoryMgtSystem.repositories.view;

import java.math.BigDecimal;

public interface ThongKeKhachHangView {
    Long getCustomerId();
    Long getTotalOrders();
    BigDecimal getTotalSpent();
    BigDecimal getOutstanding();
}
