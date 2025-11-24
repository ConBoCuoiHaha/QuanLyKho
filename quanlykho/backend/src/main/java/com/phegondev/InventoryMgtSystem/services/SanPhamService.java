package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.SanPhamDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import org.springframework.web.multipart.MultipartFile;

public interface SanPhamService {
    PhanHoi luuSanPham(SanPhamDTO sanPhamDTO, MultipartFile imageFile);

    PhanHoi capNhatSanPham(SanPhamDTO sanPhamDTO, MultipartFile imageFile);

    PhanHoi layTatCaSanPham();

    PhanHoi laySanPhamTheoId(Long id);

    PhanHoi xoaSanPham(Long id);

    PhanHoi timKiemSanPham(String tuKhoa);

    PhanHoi laySanPhamSapHetHang();

    PhanHoi layTrangSanPham(int page, int size, String keyword, Long categoryId);

    PhanHoi laySanPhamSapHetHan(int days);

    PhanHoi laySanPhamHetHan();

    PhanHoi laySanPhamTheoSku(String sku);
}
