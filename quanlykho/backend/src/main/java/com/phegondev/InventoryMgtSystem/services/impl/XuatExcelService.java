package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.GiaoDichRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class XuatExcelService {

    private final SanPhamRepository sanPhamRepository;
    private final GiaoDichRepository giaoDichRepository;

    @Transactional(readOnly = true)
    public byte[] xuatDanhSachSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("SanPham");
            taoDong(sheet.createRow(0), "ID", "Ten", "SKU", "Danh muc", "Gia", "Ton kho", "Ton toi thieu", "Giu truoc");

            int rowIndex = 1;
            for (SanPham sanPham : sanPhams) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(sanPham.getId());
                row.createCell(1).setCellValue(sanPham.getName());
                row.createCell(2).setCellValue(sanPham.getSku());
                row.createCell(3).setCellValue(sanPham.getCategory() != null ? sanPham.getCategory().getName() : "");
                row.createCell(4).setCellValue(convertToNumber(sanPham.getPrice()));
                row.createCell(5).setCellValue(defaultInt(sanPham.getStockQuantity()));
                row.createCell(6).setCellValue(defaultInt(sanPham.getMinStock()));
                row.createCell(7).setCellValue(defaultInt(sanPham.getReservedQuantity()));
            }

            for (int i = 0; i < 8; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Khong the tao file Excel san pham", e);
        }
    }

    @Transactional(readOnly = true)
    public byte[] xuatGiaoDich(LocalDate tuNgay, LocalDate denNgay) {
        List<GiaoDich> giaoDichs = layGiaoDichTheoKhoang(tuNgay, denNgay);
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("GiaoDich");
            taoDong(sheet.createRow(0), "ID", "Loai", "Trang thai", "San pham", "Nha cung cap",
                    "Khach hang", "Tong so luong", "Tong gia", "Mo ta", "Ngay tao");

            int rowIndex = 1;
            for (GiaoDich giaoDich : giaoDichs) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(giaoDich.getId());
                row.createCell(1).setCellValue(giaoDich.getTransactionType() != null ? giaoDich.getTransactionType().name() : "");
                row.createCell(2).setCellValue(giaoDich.getStatus() != null ? giaoDich.getStatus().name() : "");
                row.createCell(3).setCellValue(giaoDich.getSanPham() != null ? giaoDich.getSanPham().getName() : "");
                row.createCell(4).setCellValue(giaoDich.getNhaCungCap() != null ? giaoDich.getNhaCungCap().getName() : "");
                row.createCell(5).setCellValue(giaoDich.getCustomer() != null ? giaoDich.getCustomer().getName() : "");
                row.createCell(6).setCellValue(giaoDich.getTotalProducts() != null ? giaoDich.getTotalProducts() : 0);
                row.createCell(7).setCellValue(convertToNumber(giaoDich.getTotalPrice()));
                row.createCell(8).setCellValue(giaoDich.getDescription() != null ? giaoDich.getDescription() : "");
                row.createCell(9).setCellValue(giaoDich.getCreatedAt() != null ? giaoDich.getCreatedAt().toString() : "");
            }

            for (int i = 0; i < 10; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException("Khong the tao file Excel giao dich", e);
        }
    }

    private List<GiaoDich> layGiaoDichTheoKhoang(LocalDate tuNgay, LocalDate denNgay) {
        if (tuNgay == null && denNgay == null) {
            return giaoDichRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        LocalDate start = tuNgay != null ? tuNgay : denNgay;
        LocalDate end = denNgay != null ? denNgay : tuNgay;
        if (start.isAfter(end)) {
            LocalDate temp = start;
            start = end;
            end = temp;
        }
        LocalDateTime from = start.atStartOfDay();
        LocalDateTime to = end.atTime(23, 59, 59);
        return giaoDichRepository.findByCreatedAtBetween(from, to);
    }

    private void taoDong(Row row, String... labels) {
        for (int i = 0; i < labels.length; i++) {
            row.createCell(i).setCellValue(labels[i]);
        }
    }

    private double convertToNumber(BigDecimal value) {
        return value == null ? 0 : value.doubleValue();
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }
}
