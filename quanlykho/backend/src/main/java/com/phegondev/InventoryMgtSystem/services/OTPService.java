package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.OTPResetPasswordRequest;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface OTPService {
    PhanHoi guiOTPQuenMatKhau(String email);

    PhanHoi doiMatKhauBangOTP(OTPResetPasswordRequest request);
}
