package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.BackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN')")
public class SaoLuuController {

    private final BackupService backupService;

    @Value("${backup.dir:backups}")
    private String backupDir;

    @PostMapping("/xuat")
    public ResponseEntity<byte[]> taoBackup() throws IOException {
        PhanHoi phanHoi = backupService.taoBanSaoLuu();
        if (phanHoi.getTepTin() == null) {
            return ResponseEntity.status(500).body(null);
        }
        Path file = Path.of(phanHoi.getTepTin());
        byte[] data = Files.readAllBytes(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @GetMapping("/danh-sach")
    public ResponseEntity<List<String>> danhSach() {
        return ResponseEntity.ok(backupService.danhSachBanSaoLuu());
    }

    @GetMapping("/tai/{fileName}")
    public ResponseEntity<byte[]> taiFile(@PathVariable String fileName) throws IOException {
        String safeName = Path.of(fileName).getFileName().toString();
        Path dir = Path.of(backupDir);
        Path file = dir.resolve(safeName).normalize();
        if (!Files.exists(file) || !file.toAbsolutePath().startsWith(dir.toAbsolutePath())) {
            return ResponseEntity.notFound().build();
        }
        byte[] data = Files.readAllBytes(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getFileName())
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @PostMapping("/khoi-phuc")
    public ResponseEntity<PhanHoi> khoiPhuc(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(backupService.khoiPhucDuLieu(file));
    }
}
