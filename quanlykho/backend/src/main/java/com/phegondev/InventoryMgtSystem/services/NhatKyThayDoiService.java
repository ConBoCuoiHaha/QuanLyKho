package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

import java.time.LocalDate;

public interface NhatKyThayDoiService {

    void ghi(String module,
             String hanhDong,
             String moTa,
             String doiTuongLoai,
             Long doiTuongId,
             Object duLieuCu,
             Object duLieuMoi);

    PhanHoi timKiem(int page,
                    int size,
                    String module,
                    String hanhDong,
                    Long userId,
                    String doiTuongLoai,
                    Long doiTuongId,
                    LocalDate tuNgay,
                    LocalDate denNgay);
}
