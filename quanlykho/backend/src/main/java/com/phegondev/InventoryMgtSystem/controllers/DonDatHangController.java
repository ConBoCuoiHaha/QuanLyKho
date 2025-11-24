package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.*;
import com.phegondev.InventoryMgtSystem.services.DonDatHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/don-dat-hang")
@RequiredArgsConstructor
public class DonDatHangController {

    private final DonDatHangService donDatHangService;

    @PostMapping
    public ResponseEntity<PhanHoi> taoDon(@Valid @RequestBody DonDatHangYeuCau yeuCau) {
        return ResponseEntity.ok(donDatHangService.taoDon(yeuCau));
    }

    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<PhanHoi> capNhatTrangThai(@PathVariable Long id,
                                                    @Valid @RequestBody DonDatHangTrangThaiYeuCau yeuCau) {
        return ResponseEntity.ok(donDatHangService.capNhatTrangThai(id, yeuCau));
    }

    @GetMapping
    public ResponseEntity<PhanHoi> danhSach(@RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(name = "status", required = false) String trangThai,
                                            @RequestParam(name = "nhaCungCapId", required = false) Long nhaCungCapId) {
        return ResponseEntity.ok(donDatHangService.danhSach(page, size, trangThai, nhaCungCapId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> chiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(donDatHangService.chiTiet(id));
    }

    @PostMapping("/{id}/nhan-hang")
    public ResponseEntity<PhanHoi> nhanHang(@PathVariable Long id,
                                            @Valid @RequestBody NhanHangYeuCau yeuCau) {
        return ResponseEntity.ok(donDatHangService.nhanHang(id, yeuCau));
    }
}
