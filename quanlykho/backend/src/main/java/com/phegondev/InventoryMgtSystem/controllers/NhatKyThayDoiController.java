package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.NhatKyThayDoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nhat-ky")
@RequiredArgsConstructor
public class NhatKyThayDoiController {

    private final NhatKyThayDoiService nhatKyThayDoiService;

    @GetMapping
    public ResponseEntity<PhanHoi> timKiem(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "10") int size,
                                           @RequestParam(required = false) String module,
                                           @RequestParam(required = false) String hanhDong,
                                           @RequestParam(required = false) Long userId,
                                           @RequestParam(required = false) String doiTuongLoai,
                                           @RequestParam(required = false) Long doiTuongId,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay) {
        return ResponseEntity.ok(
                nhatKyThayDoiService.timKiem(page, size, module, hanhDong, userId, doiTuongLoai, doiTuongId, tuNgay, denNgay)
        );
    }
}
