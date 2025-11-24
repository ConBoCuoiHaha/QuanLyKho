package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/tong-quan")
    public ResponseEntity<PhanHoi> tongQuan(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "month", required = false) Integer month,
            @RequestParam(value = "lowLimit", required = false) Integer lowLimit
    ) {
        return ResponseEntity.ok(dashboardService.tongQuan(year, month, lowLimit));
    }
}
