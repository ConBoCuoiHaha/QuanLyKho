package com.phegondev.InventoryMgtSystem.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class YeuCauKiemKe {

    @NotNull(message = "San pham la bat buoc")
    private Long sanPhamId;

    private Long khoId;

    @NotNull(message = "So luong thuc te la bat buoc")
    @Min(value = 0, message = "So luong thuc te phai lon hon hoac bang 0")
    private Integer soLuongThucTe;

    private String lyDo;
    private String ghiChu;
}
