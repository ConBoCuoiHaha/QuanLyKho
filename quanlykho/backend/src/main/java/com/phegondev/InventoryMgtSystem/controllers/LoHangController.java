package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.LoHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lo-hang")
@RequiredArgsConstructor
public class LoHangController {

    private final LoHangService loHangService;

    @GetMapping("/sap-het-han")
    public ResponseEntity<PhanHoi> sapHetHan(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(loHangService.layLoSapHetHan(days));
    }

    @GetMapping("/het-han")
    public ResponseEntity<PhanHoi> hetHan() {
        return ResponseEntity.ok(loHangService.layLoHetHan());
    }

    @PostMapping("/huy/{loHangId}")
    public ResponseEntity<PhanHoi> huy(@PathVariable Long loHangId) {
        return ResponseEntity.ok(loHangService.huyLo(loHangId));
    }
}
