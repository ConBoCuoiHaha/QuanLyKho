package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;

@RestController
@RequestMapping("/api/xuat-bao-cao")
@RequiredArgsConstructor
public class XuatBaoCaoController {

    private final SanPhamService sanPhamService;

    @GetMapping("/san-pham.xlsx")
    public ResponseEntity<byte[]> exportSanPhamExcel() throws Exception {
        var response = sanPhamService.layTatCaSanPham();
        var sanPhams = response.getSanPhams();
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet("SanPham");
            int rowIdx = 0;
            Row header = sheet.createRow(rowIdx++);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Tên");
            header.createCell(2).setCellValue("SKU");
            header.createCell(3).setCellValue("Giá");
            header.createCell(4).setCellValue("Tồn kho");
            header.createCell(5).setCellValue("Tồn tối thiểu");

            if (sanPhams != null) {
                for (var p : sanPhams) {
                    Row r = sheet.createRow(rowIdx++);
                    int c = 0;
                    r.createCell(c++).setCellValue(p.getId() != null ? p.getId() : 0);
                    r.createCell(c++).setCellValue(p.getName() != null ? p.getName() : "");
                    r.createCell(c++).setCellValue(p.getSku() != null ? p.getSku() : "");
                    r.createCell(c++).setCellValue(p.getPrice() != null ? p.getPrice().doubleValue() : 0);
                    r.createCell(c++).setCellValue(p.getStockQuantity() != null ? p.getStockQuantity() : 0);
                    r.createCell(c++).setCellValue(p.getMinStock() != null ? p.getMinStock() : 0);
                }
            }
            wb.write(out);
            byte[] bytes = out.toByteArray();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=san-pham.xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(bytes);
        }
    }
}
