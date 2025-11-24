package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.models.DanhMuc;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.DanhMucRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ImportExcelService {

    private final SanPhamRepository sanPhamRepository;
    private final DanhMucRepository danhMucRepository;
    private final DataFormatter dataFormatter = new DataFormatter();

    @Transactional
    public PhanHoi importSanPham(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new NameValueRequiredException("File la bat buoc");
        }
        int thanhCong = 0;
        List<String> loi = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int last = sheet.getLastRowNum();
            for (int i = 1; i <= last; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                try {
                    xuLyDong(row);
                    thanhCong++;
                } catch (Exception ex) {
                    loi.add("Dong " + (i + 1) + ": " + ex.getMessage());
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("Khong the doc file excel", e);
        }
        return PhanHoi.builder()
                .status(200)
                .message("Nhap san pham hoan tat")
                .soLuongThanhCong(thanhCong)
                .soLuongThatBai(loi.size())
                .loiNhap(loi.isEmpty() ? null : loi)
                .build();
    }

    private void xuLyDong(Row row) {
        String name = getString(row, 0, "Ten san pham");
        String sku = getString(row, 1, "SKU");
        String category = getString(row, 2, "Danh muc");
        BigDecimal price = getBigDecimal(row, 3, "Gia");
        int stock = getInteger(row, 4, "Ton kho");
        int minStock = getInteger(row, 5, "Ton toi thieu");

        DanhMuc danhMuc = danhMucRepository.findByNameIgnoreCase(category.trim())
                .orElseGet(() -> danhMucRepository.save(DanhMuc.builder().name(category.trim()).build()));

        Optional<SanPham> sanPhamOptional = sanPhamRepository.findBySkuIgnoreCase(sku.trim());
        SanPham sanPham = sanPhamOptional.orElseGet(SanPham::new);
        sanPham.setName(name.trim());
        sanPham.setSku(sku.trim());
        sanPham.setCategory(danhMuc);
        sanPham.setPrice(price);
        sanPham.setStockQuantity(stock);
        sanPham.setMinStock(minStock);
        sanPhamRepository.save(sanPham);
    }

    private String getString(Row row, int cellIndex, String field) {
        Cell cell = row.getCell(cellIndex);
        String value = cell != null ? dataFormatter.formatCellValue(cell) : "";
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " khong duoc de trong");
        }
        return value;
    }

    private BigDecimal getBigDecimal(Row row, int cellIndex, String field) {
        Cell cell = row.getCell(cellIndex);
        String value = cell != null ? dataFormatter.formatCellValue(cell) : "";
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " khong duoc de trong");
        }
        try {
            return new BigDecimal(value.replace(",", ""));
        } catch (Exception ex) {
            throw new IllegalArgumentException(field + " khong hop le");
        }
    }

    private int getInteger(Row row, int cellIndex, String field) {
        Cell cell = row.getCell(cellIndex);
        String value = cell != null ? dataFormatter.formatCellValue(cell) : "";
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(field + " khong duoc de trong");
        }
        try {
            return (int) Double.parseDouble(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException(field + " khong hop le");
        }
    }
}
