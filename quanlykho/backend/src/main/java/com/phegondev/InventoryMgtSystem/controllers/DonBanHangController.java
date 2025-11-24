package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.*;
import com.phegondev.InventoryMgtSystem.services.DonBanHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/don-ban-hang")
@RequiredArgsConstructor
public class DonBanHangController {

    private final DonBanHangService donBanHangService;

    @PostMapping
    public ResponseEntity<PhanHoi> taoDon(@RequestBody @Valid DonBanHangYeuCau yeuCau) {
        return ResponseEntity.ok(donBanHangService.taoDon(yeuCau));
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<PhanHoi> capNhatTrangThai(@PathVariable Long id,
                                                    @RequestBody @Valid DonBanHangTrangThaiYeuCau yeuCau) {
        return ResponseEntity.ok(donBanHangService.capNhatTrangThai(id, yeuCau));
    }

    @GetMapping
    public ResponseEntity<PhanHoi> danhSach(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(name = "status", required = false) String trangThai,
                                            @RequestParam(name = "khachHangId", required = false) Long khachHangId) {
        return ResponseEntity.ok(donBanHangService.danhSach(page, size, trangThai, khachHangId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> chiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(donBanHangService.chiTiet(id));
    }
}
