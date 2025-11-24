package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauKiemKe;
import com.phegondev.InventoryMgtSystem.services.KiemKeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kiem-ke")
@RequiredArgsConstructor
public class KiemKeController {

    private final KiemKeService kiemKeService;

    @PostMapping("/thuc-hien")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    public ResponseEntity<PhanHoi> thucHien(@RequestBody @Valid YeuCauKiemKe yeuCau) {
        return ResponseEntity.ok(kiemKeService.thucHienKiemKe(yeuCau));
    }

    @GetMapping("/lich-su")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    public ResponseEntity<PhanHoi> lichSu(@RequestParam(required = false) Long sanPhamId,
                                          @RequestParam(required = false) Long khoId,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(kiemKeService.layLichSuKiemKe(sanPhamId, khoId, page, size));
    }

    @GetMapping("/hien-trang")
    @PreAuthorize("hasAnyAuthority('ADMIN','MANAGER')")
    public ResponseEntity<PhanHoi> hienTrang(@RequestParam Long sanPhamId,
                                             @RequestParam(required = false) Long khoId) {
        return ResponseEntity.ok(kiemKeService.hienTrangTonKho(sanPhamId, khoId));
    }
}
