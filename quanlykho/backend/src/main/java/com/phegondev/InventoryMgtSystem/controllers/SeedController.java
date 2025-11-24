package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.SeedDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SeedController {

    private final SeedDataService seedDataService;

    @PostMapping("/demo")
    public ResponseEntity<PhanHoi> taoLaiDemo(@RequestParam(defaultValue = "false") boolean force) {
        return ResponseEntity.ok(seedDataService.taoDuLieuDemo(force));
    }
}
