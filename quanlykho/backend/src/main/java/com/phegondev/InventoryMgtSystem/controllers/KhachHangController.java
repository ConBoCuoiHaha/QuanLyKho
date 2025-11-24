package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.KhachHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.KhachHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangService khachHangService;

    @PostMapping("/add")
    public ResponseEntity<PhanHoi> add(@RequestBody @Valid KhachHangDTO dto) {
        return ResponseEntity.ok(khachHangService.add(dto));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<PhanHoi> update(@PathVariable Long id, @RequestBody @Valid KhachHangDTO dto) {
        return ResponseEntity.ok(khachHangService.update(id, dto));
    }

    @GetMapping("/all")
    public ResponseEntity<PhanHoi> all() {
        return ResponseEntity.ok(khachHangService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> get(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.getById(id));
    }

    @GetMapping("/chi-tiet/{id}")
    public ResponseEntity<PhanHoi> detail(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.detail(id));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<PhanHoi> delete(@PathVariable Long id) {
        return ResponseEntity.ok(khachHangService.delete(id));
    }
}
