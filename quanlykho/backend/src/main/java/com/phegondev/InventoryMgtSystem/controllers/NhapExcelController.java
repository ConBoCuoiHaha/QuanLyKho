package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.impl.ImportExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/nhap-excel")
@RequiredArgsConstructor
public class NhapExcelController {

    private final ImportExcelService importExcelService;

    @PostMapping(value = "/san-pham", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhanHoi> importSanPham(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(importExcelService.importSanPham(file));
    }
}
