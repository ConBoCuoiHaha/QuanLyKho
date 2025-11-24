package com.phegondev.InventoryMgtSystem.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NhatKyDTO {
    private Long id;
    private String module;
    private String hanhDong;
    private String moTa;
    private String doiTuongLoai;
    private Long doiTuongId;
    private String duLieuCu;
    private String duLieuMoi;
    private LocalDateTime createdAt;
    private UserDTO nguoiThucHien;
}
