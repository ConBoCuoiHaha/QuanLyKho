package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.SanPhamDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;

    @PostMapping("/them")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> themSanPham(
            @RequestParam("imageFile") MultipartFile imageFile,
            @RequestParam("name") String name,
            @RequestParam("sku") String sku,
            @RequestParam("price") BigDecimal price,
            @RequestParam("stockQuantity") Integer stockQuantity,
            @RequestParam("categoryId") Long categoryId,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "description", required = false) String description
    ) {
        SanPhamDTO sanPhamDTO = new SanPhamDTO();
        sanPhamDTO.setName(name);
        sanPhamDTO.setSku(sku);
        sanPhamDTO.setPrice(price);
        sanPhamDTO.setStockQuantity(stockQuantity);
        sanPhamDTO.setCategoryId(categoryId);
        sanPhamDTO.setMinStock(minStock);
        sanPhamDTO.setDescription(description);

        return ResponseEntity.ok(sanPhamService.luuSanPham(sanPhamDTO, imageFile));
    }

    @PutMapping("/cap-nhat")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> capNhatSanPham(
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value = "price", required = false) BigDecimal price,
            @RequestParam(value = "stockQuantity", required = false) Integer stockQuantity,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "minStock", required = false) Integer minStock,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("sanPhamId") Long sanPhamId
    ) {
        SanPhamDTO sanPhamDTO = new SanPhamDTO();
        sanPhamDTO.setId(sanPhamId);
        sanPhamDTO.setName(name);
        sanPhamDTO.setSku(sku);
        sanPhamDTO.setPrice(price);
        sanPhamDTO.setStockQuantity(stockQuantity);
        sanPhamDTO.setCategoryId(categoryId);
        sanPhamDTO.setMinStock(minStock);
        sanPhamDTO.setDescription(description);

        return ResponseEntity.ok(sanPhamService.capNhatSanPham(sanPhamDTO, imageFile));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<PhanHoi> layTatCaSanPham() {
        return ResponseEntity.ok(sanPhamService.layTatCaSanPham());
    }

    @GetMapping("/phan-trang")
    public ResponseEntity<PhanHoi> layTrangSanPham(@RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size,
                                                   @RequestParam(required = false) String keyword,
                                                   @RequestParam(required = false) Long categoryId) {
        return ResponseEntity.ok(sanPhamService.layTrangSanPham(page, size, keyword, categoryId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> laySanPhamTheoId(@PathVariable Long id) {
        return ResponseEntity.ok(sanPhamService.laySanPhamTheoId(id));
    }

    @GetMapping("/ma-vach/{sku}")
    public ResponseEntity<PhanHoi> laySanPhamTheoSku(@PathVariable String sku) {
        return ResponseEntity.ok(sanPhamService.laySanPhamTheoSku(sku));
    }

    @DeleteMapping("/xoa/{id}")
    public ResponseEntity<PhanHoi> xoaSanPham(@PathVariable Long id) {
        return ResponseEntity.ok(sanPhamService.xoaSanPham(id));
    }

    @GetMapping("/tim-kiem")
    public ResponseEntity<PhanHoi> timKiemSanPham(@RequestParam String input) {
        return ResponseEntity.ok(sanPhamService.timKiemSanPham(input));
    }

    @GetMapping("/sap-het-hang")
    public ResponseEntity<PhanHoi> sanPhamSapHetHang() {
        return ResponseEntity.ok(sanPhamService.laySanPhamSapHetHang());
    }

    @GetMapping("/sap-het-han")
    public ResponseEntity<PhanHoi> sanPhamSapHetHan(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(sanPhamService.laySanPhamSapHetHan(days));
    }

    @GetMapping("/het-han")
    public ResponseEntity<PhanHoi> sanPhamHetHan() {
        return ResponseEntity.ok(sanPhamService.laySanPhamHetHan());
    }
}
