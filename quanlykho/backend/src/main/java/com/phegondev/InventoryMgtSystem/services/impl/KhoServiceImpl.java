package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.KhoDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhoThongKeDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.TonKhoDTO;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.Kho;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.models.TonKho;
import com.phegondev.InventoryMgtSystem.repositories.KhoRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import com.phegondev.InventoryMgtSystem.repositories.TonKhoRepository;
import com.phegondev.InventoryMgtSystem.services.KhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhoServiceImpl implements KhoService {

    private final KhoRepository khoRepository;
    private final SanPhamRepository sanPhamRepository;
    private final TonKhoRepository tonKhoRepository;

    @Override
    public PhanHoi create(String name, String address, String manager) {
        if (name == null || name.isBlank()) {
            throw new NameValueRequiredException("Ten kho la bat buoc");
        }
        Kho kho = Kho.builder().name(name).address(address).manager(manager).build();
        khoRepository.save(kho);
        return PhanHoi.builder().status(200).message("Tao kho thanh cong").build();
    }

    @Override
    public PhanHoi getAll() {
        List<KhoDTO> khoDTOList = khoRepository.findAll().stream()
                .map(kho -> new KhoDTO(kho.getId(), kho.getName(), kho.getAddress(), kho.getManager()))
                .toList();
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .khos(khoDTOList)
                .build();
    }

    @Override
    @Transactional
    public PhanHoi transfer(Long sanPhamId, Long fromWarehouseId, Long toWarehouseId, Integer quantity) {
        if (quantity == null || quantity <= 0) {
            throw new NameValueRequiredException("So luong khong hop le");
        }
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Kho khoNguon = khoRepository.findById(fromWarehouseId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay kho nguon"));
        Kho khoDich = khoRepository.findById(toWarehouseId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay kho dich"));

        TonKho tonKhoNguon = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, khoNguon)
                .orElseThrow(() -> new NotFoundException("Khong co ton kho o kho nguon"));
        if (tonKhoNguon.getQuantity() < quantity) {
            throw new NameValueRequiredException("Khong du hang tai kho nguon");
        }
        tonKhoNguon.setQuantity(tonKhoNguon.getQuantity() - quantity);
        tonKhoRepository.save(tonKhoNguon);

        TonKho tonKhoDich = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, khoDich)
                .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(khoDich).quantity(0).build());
        tonKhoDich.setQuantity((tonKhoDich.getQuantity() == null ? 0 : tonKhoDich.getQuantity()) + quantity);
        tonKhoRepository.save(tonKhoDich);

        return PhanHoi.builder().status(200).message("Chuyen kho thanh cong").build();
    }

    @Override
    public PhanHoi thongTinTonKho(Long sanPhamId, Long khoId) {
        List<TonKho> tonKhos;
        if (sanPhamId != null && khoId != null) {
            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
            Kho kho = khoRepository.findById(khoId)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                    .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(kho).quantity(0).build());
            tonKhos = List.of(tonKho);
        } else if (sanPhamId != null) {
            SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
            tonKhos = tonKhoRepository.findBySanPham(sanPham);
            if (tonKhos.isEmpty()) {
                tonKhos = List.of(TonKho.builder().sanPham(sanPham).warehouse(null).quantity(sanPham.getStockQuantity()).build());
            }
        } else if (khoId != null) {
            Kho kho = khoRepository.findById(khoId)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
            tonKhos = tonKhoRepository.findByWarehouse(kho);
        } else {
            tonKhos = tonKhoRepository.findAll();
        }

        List<TonKhoDTO> dtoList = tonKhos.stream()
                .map(this::mapTonKho)
                .sorted(Comparator.comparing(TonKhoDTO::getTenSanPham, Comparator.nullsLast(String::compareToIgnoreCase)))
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .tonKhos(dtoList)
                .build();
    }

    @Override
    public PhanHoi thongKeTongTheoKho() {
        List<TonKho> tonKhoList = tonKhoRepository.findAll();
        Map<Long, List<TonKho>> tonTheoKho = tonKhoList.stream()
                .filter(t -> t.getWarehouse() != null)
                .collect(Collectors.groupingBy(t -> t.getWarehouse().getId()));

        List<KhoThongKeDTO> dtoList = khoRepository.findAll().stream()
                .map(kho -> {
                    List<TonKho> danhSach = tonTheoKho.getOrDefault(kho.getId(), List.of());
                    int tong = danhSach.stream()
                            .mapToInt(t -> t.getQuantity() == null ? 0 : t.getQuantity())
                            .sum();
                    int soSanPham = (int) danhSach.stream()
                            .filter(t -> t.getQuantity() != null && t.getQuantity() > 0)
                            .count();
                    return KhoThongKeDTO.builder()
                            .khoId(kho.getId())
                            .tenKho(kho.getName())
                            .tongSoLuong(tong)
                            .soSanPham(soSanPham)
                            .build();
                })
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .thongKeKhos(dtoList)
                .build();
    }

    private TonKhoDTO mapTonKho(TonKho tonKho) {
        SanPham sanPham = tonKho.getSanPham();
        Kho kho = tonKho.getWarehouse();
        return TonKhoDTO.builder()
                .sanPhamId(sanPham != null ? sanPham.getId() : null)
                .tenSanPham(sanPham != null ? sanPham.getName() : null)
                .sku(sanPham != null ? sanPham.getSku() : null)
                .khoId(kho != null ? kho.getId() : null)
                .tenKho(kho != null ? kho.getName() : "Tong")
                .soLuong(tonKho.getQuantity() == null ? 0 : tonKho.getQuantity())
                .build();
    }
}
