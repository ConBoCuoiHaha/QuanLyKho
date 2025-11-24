package com.phegondev.InventoryMgtSystem.dtos.backup;

import com.phegondev.InventoryMgtSystem.dtos.CategoryDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhachHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhoDTO;
import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DuLieuSaoLuuDTO {
    private List<CategoryDTO> danhMucs;
    private List<NhaCungCapDTO> nhaCungCaps;
    private List<KhachHangDTO> khachHangs;
    private List<KhoDTO> khos;
    private List<SanPhamBackupDTO> sanPhams;
    private List<TonKhoBackupDTO> tonKhos;
    private List<LoHangBackupDTO> loHangs;
    private List<GiaoDichBackupDTO> giaoDichs;
    private List<UserBackupDTO> nguoiDungs;
}
