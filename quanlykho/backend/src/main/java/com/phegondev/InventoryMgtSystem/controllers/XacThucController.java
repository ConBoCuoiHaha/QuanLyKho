package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.LoginRequest;
import com.phegondev.InventoryMgtSystem.dtos.OTPRequest;
import com.phegondev.InventoryMgtSystem.dtos.OTPResetPasswordRequest;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.RegisterRequest;
import com.phegondev.InventoryMgtSystem.services.OTPService;
import com.phegondev.InventoryMgtSystem.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class XacThucController {

    private final UserService userService;
    private final OTPService otpService;

    @PostMapping("/register")
    public ResponseEntity<PhanHoi> registerUser(@RequestBody @Valid RegisterRequest registerRequest) {
        return ResponseEntity.ok(userService.registerUser(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<PhanHoi> loginUser(@RequestBody @Valid LoginRequest loginRequest) {
        return ResponseEntity.ok(userService.loginUser(loginRequest));
    }

    @PostMapping("/quen-mat-khau/otp")
    public ResponseEntity<PhanHoi> requestOtp(@RequestBody OTPRequest body) {
        return ResponseEntity.ok(otpService.guiOTPQuenMatKhau(body.getEmail()));
    }

    @PostMapping("/quen-mat-khau/dat-mat-khau")
    public ResponseEntity<PhanHoi> resetPasswordWithOtp(@RequestBody OTPResetPasswordRequest body) {
        return ResponseEntity.ok(otpService.doiMatKhauBangOTP(body));
    }

}
