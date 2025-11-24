package com.phegondev.InventoryMgtSystem.controllers;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.services.KhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kho")
@RequiredArgsConstructor
public class KhoController {

    private final KhoService khoService;

    @PostMapping(value = "/them", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<PhanHoi> add(@RequestParam String name, @RequestParam(required = false) String address, @RequestParam(required = false) String manager) {
        return ResponseEntity.ok(khoService.create(name, address, manager));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PhanHoi> addJson(@RequestBody(required = false) com.phegondev.InventoryMgtSystem.dtos.KhoDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("Thong tin kho la bat buoc");
        }
        return ResponseEntity.ok(khoService.create(dto.getName(), dto.getAddress(), dto.getManager()));
    }

    @GetMapping("/tat-ca")
    public ResponseEntity<PhanHoi> all() {
        return ResponseEntity.ok(khoService.getAll());
    }

    @GetMapping("/ton-kho")
    public ResponseEntity<PhanHoi> tonKho(@RequestParam(required = false) Long sanPhamId,
                                          @RequestParam(required = false) Long khoId) {
        return ResponseEntity.ok(khoService.thongTinTonKho(sanPhamId, khoId));
    }

    @GetMapping("/thong-ke")
    public ResponseEntity<PhanHoi> thongKe() {
        return ResponseEntity.ok(khoService.thongKeTongTheoKho());
    }

    @PostMapping("/chuyen-kho")
    public ResponseEntity<PhanHoi> transfer(@RequestParam Long sanPhamId,
                                            @RequestParam Long fromWarehouseId,
                                            @RequestParam Long toWarehouseId,
                                            @RequestParam Integer quantity) {
        return ResponseEntity.ok(khoService.transfer(sanPhamId, fromWarehouseId, toWarehouseId, quantity));
    }
}
