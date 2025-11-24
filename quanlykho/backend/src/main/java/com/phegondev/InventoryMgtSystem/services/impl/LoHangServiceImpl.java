package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.LoHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.LoHang;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.LoHangRepository;
import com.phegondev.InventoryMgtSystem.services.LoHangQuanLy;
import com.phegondev.InventoryMgtSystem.services.LoHangService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoHangServiceImpl implements LoHangService {

    private final LoHangRepository loHangRepository;
    private final ModelMapper modelMapper;
    private final LoHangQuanLy loHangQuanLy;

    @Override
    public PhanHoi layLoSapHetHan(int days) {
        LocalDate from = LocalDate.now();
        LocalDate to = from.plusDays(days);
        List<LoHangDTO> dtoList = loHangRepository.findExpiringBetween(from, to).stream()
                .filter(lo -> lo.getQuantityRemaining() != null && lo.getQuantityRemaining() > 0)
                .sorted(Comparator.comparing(LoHang::getExpiryDate, Comparator.nullsLast(LocalDate::compareTo)))
                .map(lo -> modelMapper.map(lo, LoHangDTO.class))
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .loHangs(dtoList)
                .build();
    }

    @Override
    public PhanHoi layLoHetHan() {
        List<LoHangDTO> dtoList = loHangRepository.findExpired(LocalDate.now()).stream()
                .filter(lo -> lo.getQuantityRemaining() != null && lo.getQuantityRemaining() > 0)
                .sorted(Comparator.comparing(LoHang::getExpiryDate, Comparator.nullsLast(LocalDate::compareTo)))
                .map(lo -> modelMapper.map(lo, LoHangDTO.class))
                .toList();
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .loHangs(dtoList)
                .build();
    }

    @Override
    public PhanHoi huyLo(Long loHangId) {
        LoHang loHang = loHangRepository.findById(loHangId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay lo hang"));
        Integer remaining = loHang.getQuantityRemaining();
        if (remaining == null || remaining == 0) {
            return PhanHoi.builder().status(200).message("Lo hang da rong").build();
        }
        SanPham sanPham = loHang.getSanPham();
        int hienCo = sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity();
        sanPham.setStockQuantity(Math.max(0, hienCo - remaining));
        loHang.setQuantityRemaining(0);
        loHangRepository.save(loHang);
        loHangQuanLy.capNhatHanSanPham(sanPham);
        return PhanHoi.builder().status(200).message("Da huy lo hang het han").build();
    }
}
