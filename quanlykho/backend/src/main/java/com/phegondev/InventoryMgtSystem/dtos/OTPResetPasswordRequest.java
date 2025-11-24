package com.phegondev.InventoryMgtSystem.dtos;

import lombok.Data;

@Data
public class OTPResetPasswordRequest {
    private String email;
    private String otp;
    private String matKhauMoi;
    private String xacNhanMatKhau;
}
