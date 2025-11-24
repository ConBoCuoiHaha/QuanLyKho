package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.services.impl.XuatPdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/xuat-pdf")
@RequiredArgsConstructor
public class XuatPdfController {

    private final XuatPdfService xuatPdfService;

    @GetMapping("/hoa-don-giao-dich/{id}")
    public ResponseEntity<byte[]> xuatHoaDonGiaoDich(@PathVariable("id") Long giaoDichId) {
        byte[] data = xuatPdfService.xuatHoaDonGiaoDich(giaoDichId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=hoa-don-giao-dich-" + giaoDichId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }
}
