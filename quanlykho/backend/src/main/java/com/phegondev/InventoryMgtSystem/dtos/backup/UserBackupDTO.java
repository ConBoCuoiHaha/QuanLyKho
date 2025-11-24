package com.phegondev.InventoryMgtSystem.dtos.backup;

import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserBackupDTO {
    private String name;
    private String email;
    private String password;
    private String phoneNumber;
    private VaiTroNguoiDung role;
}
