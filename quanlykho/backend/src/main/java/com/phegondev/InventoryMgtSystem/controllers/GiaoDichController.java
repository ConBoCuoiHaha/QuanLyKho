package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.services.GiaoDichService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/giao-dich")
@RequiredArgsConstructor
public class GiaoDichController {

    private final GiaoDichService giaoDichService;

    @PostMapping("/nhap")
    public ResponseEntity<PhanHoi> nhapKho(@RequestBody @Valid YeuCauGiaoDich yeuCau) {
        return ResponseEntity.ok(giaoDichService.nhapKho(yeuCau));
    }

    @PostMapping("/ban")
    public ResponseEntity<PhanHoi> banHang(@RequestBody @Valid YeuCauGiaoDich yeuCau) {
        return ResponseEntity.ok(giaoDichService.banHang(yeuCau));
    }

    @PostMapping("/tra-nha-cung-cap")
    public ResponseEntity<PhanHoi> traVeNhaCungCap(@RequestBody @Valid YeuCauGiaoDich yeuCau) {
        return ResponseEntity.ok(giaoDichService.traVeNhaCungCap(yeuCau));
    }

    @PostMapping("/khach-tra")
    public ResponseEntity<PhanHoi> khachHangTra(@RequestBody @Valid YeuCauGiaoDich yeuCau) {
        return ResponseEntity.ok(giaoDichService.khachHangTra(yeuCau));
    }

    @PostMapping("/chuyen-kho")
    public ResponseEntity<PhanHoi> chuyenKho(@RequestBody @Valid YeuCauGiaoDich yeuCau) {
        return ResponseEntity.ok(giaoDichService.chuyenKho(yeuCau));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<PhanHoi> layTatCa(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String filter) {
        return ResponseEntity.ok(giaoDichService.layTatCaGiaoDich(page, size, filter));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> layTheoId(@PathVariable Long id) {
        return ResponseEntity.ok(giaoDichService.layGiaoDichTheoId(id));
    }

    @GetMapping("/theo-thang-nam")
    public ResponseEntity<PhanHoi> layTheoThangNam(
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(giaoDichService.layGiaoDichTheoThangNam(month, year));
    }

    @PutMapping("/cap-nhat-trang-thai/{giaoDichId}")
    public ResponseEntity<PhanHoi> capNhatTrangThai(
            @PathVariable Long giaoDichId,
            @RequestBody TrangThaiGiaoDich status) {

        return ResponseEntity.ok(giaoDichService.capNhatTrangThai(giaoDichId, status));
    }
}
