package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.OTPResetPasswordRequest;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.exceptions.ValidationException;
import com.phegondev.InventoryMgtSystem.models.OTPReset;
import com.phegondev.InventoryMgtSystem.models.User;
import com.phegondev.InventoryMgtSystem.repositories.OTPResetRepository;
import com.phegondev.InventoryMgtSystem.repositories.UserRepository;
import com.phegondev.InventoryMgtSystem.services.MailService;
import com.phegondev.InventoryMgtSystem.services.OTPService;
import com.phegondev.InventoryMgtSystem.utils.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPServiceImpl implements OTPService {

    private final OTPResetRepository otpResetRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    @Value("${otp.expiration-minutes:5}")
    private int expirationMinutes;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public PhanHoi guiOTPQuenMatKhau(String email) {
        String normalizedEmail = safeTrim(email);
        if (normalizedEmail.isEmpty()) {
            throw new ValidationException("Email khong duoc de trong");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new NotFoundException("Email khong ton tai trong he thong"));

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(expirationMinutes);

        OTPReset otpReset = otpResetRepository.findByEmail(normalizedEmail)
                .orElse(OTPReset.builder().email(normalizedEmail).build());
        otpReset.setOtp(otp);
        otpReset.setExpiresAt(expiresAt);
        otpReset.setUsed(false);
        otpResetRepository.save(otpReset);

        mailService.guiOTP(user.getEmail(), otp);
        log.info("OTP gui toi email {} thanh cong", user.getEmail());

        return PhanHoi.builder()
                .status(200)
                .message("Da tao OTP. Vui long kiem tra console server (demo).")
                .build();
    }

    @Override
    public PhanHoi doiMatKhauBangOTP(OTPResetPasswordRequest request) {
        if (request == null) {
            throw new ValidationException("Yeu cau dat lai mat khau khong hop le");
        }

        String email = safeTrim(request.getEmail());
        String otp = safeTrim(request.getOtp());
        String newPassword = safeTrim(request.getMatKhauMoi());
        String confirmPassword = safeTrim(request.getXacNhanMatKhau());

        if (email.isEmpty() || otp.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            throw new ValidationException("Vui long nhap day du thong tin");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Mat khau moi va xac nhan khong khop");
        }

        OTPReset otpReset = otpResetRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("OTP khong ton tai hoac email sai"));

        if (otpReset.isUsed()) {
            throw new ValidationException("OTP da duoc su dung, vui long yeu cau OTP moi");
        }
        if (otpReset.isExpired()) {
            throw new ValidationException("OTP da het han, vui long yeu cau OTP moi");
        }
        if (!otpReset.getOtp().equals(otp)) {
            throw new ValidationException("OTP khong chinh xac");
        }

        PasswordValidator.validate(newPassword);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Khong tim thay tai khoan voi email nay"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        otpReset.setUsed(true);
        otpResetRepository.save(otpReset);

        return PhanHoi.builder()
                .status(200)
                .message("Dat lai mat khau thanh cong")
                .build();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
