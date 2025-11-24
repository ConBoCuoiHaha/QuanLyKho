package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BackupService {
    PhanHoi taoBanSaoLuu();

    PhanHoi khoiPhucDuLieu(MultipartFile file);

    List<String> danhSachBanSaoLuu();
}
