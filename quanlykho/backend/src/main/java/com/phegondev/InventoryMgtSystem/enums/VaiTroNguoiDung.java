package com.phegondev.InventoryMgtSystem.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum VaiTroNguoiDung {
    ADMIN(EnumSet.allOf(QuyenTacVu.class)),
    MANAGER(EnumSet.allOf(QuyenTacVu.class)),
    WAREHOUSE_STAFF(EnumSet.of(
            QuyenTacVu.SAN_PHAM_READ,
            QuyenTacVu.DANH_MUC_READ,
            QuyenTacVu.NHA_CUNG_CAP_READ,
            QuyenTacVu.KHO_READ,
            QuyenTacVu.KHO_UPDATE,
            QuyenTacVu.KHO_CREATE,
            QuyenTacVu.GIAO_DICH_CREATE,
            QuyenTacVu.GIAO_DICH_READ,
            QuyenTacVu.GIAO_DICH_UPDATE,
            QuyenTacVu.LO_HANG_READ,
            QuyenTacVu.LO_HANG_UPDATE,
            QuyenTacVu.LO_HANG_CREATE,
            QuyenTacVu.DON_DAT_HANG_CREATE,
            QuyenTacVu.DON_DAT_HANG_READ,
            QuyenTacVu.DON_DAT_HANG_UPDATE,
            QuyenTacVu.DON_BAN_HANG_READ,
            QuyenTacVu.KIEM_KE_CREATE,
            QuyenTacVu.KIEM_KE_READ,
            QuyenTacVu.KIEM_KE_UPDATE,
            QuyenTacVu.MA_VACH_READ,
            QuyenTacVu.EXCEL_EXPORT,
            QuyenTacVu.DASHBOARD_READ,
            QuyenTacVu.USER_READ
    )),
    ACCOUNTANT(EnumSet.of(
            QuyenTacVu.BAO_CAO_READ,
            QuyenTacVu.DASHBOARD_READ,
            QuyenTacVu.GIAO_DICH_READ,
            QuyenTacVu.DON_BAN_HANG_READ,
            QuyenTacVu.DON_DAT_HANG_READ,
            QuyenTacVu.SAN_PHAM_READ,
            QuyenTacVu.KHO_READ,
            QuyenTacVu.NHA_CUNG_CAP_READ,
            QuyenTacVu.KHACH_HANG_READ,
            QuyenTacVu.USER_READ
    )),
    SALE_STAFF(EnumSet.of(
            QuyenTacVu.SAN_PHAM_READ,
            QuyenTacVu.KHACH_HANG_CREATE,
            QuyenTacVu.KHACH_HANG_READ,
            QuyenTacVu.KHACH_HANG_UPDATE,
            QuyenTacVu.DON_BAN_HANG_CREATE,
            QuyenTacVu.DON_BAN_HANG_READ,
            QuyenTacVu.DON_BAN_HANG_UPDATE,
            QuyenTacVu.GIAO_DICH_CREATE,
            QuyenTacVu.GIAO_DICH_READ,
            QuyenTacVu.GIAO_DICH_UPDATE,
            QuyenTacVu.MA_VACH_READ,
            QuyenTacVu.DASHBOARD_READ,
            QuyenTacVu.USER_READ
    ));

    private final Set<QuyenTacVu> quyenHan;

    VaiTroNguoiDung(Set<QuyenTacVu> quyenHan) {
        this.quyenHan = Collections.unmodifiableSet(quyenHan);
    }

    public Set<QuyenTacVu> getPermissions() {
        return quyenHan;
    }
}
