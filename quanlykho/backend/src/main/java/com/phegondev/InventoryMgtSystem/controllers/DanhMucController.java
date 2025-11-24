package com.phegondev.InventoryMgtSystem.controllers;


import com.phegondev.InventoryMgtSystem.dtos.CategoryDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/danh-muc")
@RequiredArgsConstructor
public class DanhMucController {

    private final CategoryService categoryService;

    @PostMapping("/them")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> createCategory(@RequestBody @Valid CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.createCategory(categoryDTO));
    }


    @GetMapping("/tat-ca")
    public ResponseEntity<PhanHoi> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhanHoi> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/cap-nhat/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> updateUser(@PathVariable Long id, @RequestBody @Valid CategoryDTO categoryDTO) {
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryDTO));
    }

    @DeleteMapping("/xoa/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<PhanHoi> deleteCategory(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.deleteCategory(id));
    }


}
