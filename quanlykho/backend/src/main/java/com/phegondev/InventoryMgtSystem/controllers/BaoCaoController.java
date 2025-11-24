package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.BaoCaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/bao-cao")
@RequiredArgsConstructor
public class BaoCaoController {

    private final BaoCaoService baoCaoService;

    @GetMapping("/gia-tri-ton/tong")
    public ResponseEntity<PhanHoi> inventoryValuation() {
        return ResponseEntity.ok(baoCaoService.inventoryValuation());
    }

    @GetMapping("/gia-tri-ton/danh-muc")
    public ResponseEntity<PhanHoi> inventoryValuationByCategory() {
        return ResponseEntity.ok(baoCaoService.inventoryValuationByCategory());
    }

    @GetMapping("/gia-tri-ton/kho")
    public ResponseEntity<PhanHoi> inventoryValuationByWarehouse() {
        return ResponseEntity.ok(baoCaoService.inventoryValuationByWarehouse());
    }

    @GetMapping("/xuat-nhap-ton")
    public ResponseEntity<PhanHoi> stockMovement(
            @RequestParam("tuNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("denNgay") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "danhMucId", required = false) Long categoryId
    ) {
        return ResponseEntity.ok(baoCaoService.stockMovement(from, to, categoryId));
    }

    @GetMapping("/hang-ton-lau")
    public ResponseEntity<PhanHoi> hangTonLau(
            @RequestParam(value = "minDays", defaultValue = "30") int minDays
    ) {
        return ResponseEntity.ok(baoCaoService.agingInventory(minDays));
    }

    @GetMapping("/abc")
    public ResponseEntity<PhanHoi> abcAnalysis(
            @RequestParam(value = "tuNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "denNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(baoCaoService.abcAnalysis(from, to));
    }

    @GetMapping("/san-pham-ban-chay")
    public ResponseEntity<PhanHoi> bestSellers(
            @RequestParam(value = "tuNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "denNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "limit", defaultValue = "10") int limit,
            @RequestParam(value = "metric", defaultValue = "quantity") String metric
    ) {
        return ResponseEntity.ok(baoCaoService.bestSellers(from, to, limit, metric));
    }

    @GetMapping("/doanh-thu-loi-nhuan")
    public ResponseEntity<PhanHoi> revenueProfit(
            @RequestParam(value = "tuNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "denNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "interval", defaultValue = "DAY") String interval
    ) {
        return ResponseEntity.ok(baoCaoService.revenueProfit(from, to, interval));
    }

    @GetMapping("/nha-cung-cap")
    public ResponseEntity<PhanHoi> supplierReport(
            @RequestParam(value = "tuNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "denNgay", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "limit", defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(baoCaoService.supplierReport(from, to, limit));
    }
}
