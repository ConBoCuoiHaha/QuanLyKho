package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.NhaCungCapService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/nha-cung-cap")
@RequiredArgsConstructor
public class NhaCungCapController {

    private final NhaCungCapService nhaCungCapService;

    @PostMapping("/them")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> them(@RequestBody @Valid NhaCungCapDTO dto) {
        return ResponseEntity.ok(nhaCungCapService.themNhaCungCap(dto));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<PhanHoi> layTatCa() {
        return ResponseEntity.ok(nhaCungCapService.layTatCaNhaCungCap());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> layTheoId(@PathVariable Long id) {
        return ResponseEntity.ok(nhaCungCapService.layNhaCungCapTheoId(id));
    }

    @PutMapping("/cap-nhat/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> capNhat(@PathVariable Long id, @RequestBody @Valid NhaCungCapDTO dto) {
        return ResponseEntity.ok(nhaCungCapService.capNhatNhaCungCap(id, dto));
    }

    @DeleteMapping("/xoa/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> xoa(@PathVariable Long id) {
        return ResponseEntity.ok(nhaCungCapService.xoaNhaCungCap(id));
    }
}
