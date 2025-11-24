package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.models.LoHang;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.LoHangRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class LoHangQuanLy {

    private final LoHangRepository loHangRepository;

    public void addNewLot(SanPham sanPham, int soLuong, String soLo, LocalDate ngayNhap, LocalDate hanSuDung) {
        if (soLuong <= 0) {
            return;
        }
        LoHang loHang = LoHang.builder()
                .sanPham(sanPham)
                .lotNumber(buildLotNumber(sanPham, soLo))
                .receivedDate(ngayNhap != null ? ngayNhap : LocalDate.now())
                .expiryDate(hanSuDung)
                .quantityRemaining(soLuong)
                .build();
        loHangRepository.save(loHang);
        capNhatHanSanPham(sanPham);
    }

    public void addBackToLot(SanPham sanPham, int soLuong, String soLo, LocalDate ngayNhap, LocalDate hanSuDung) {
        if (soLuong <= 0) {
            return;
        }
        String lotNumber = sanitizeLotNumber(soLo);
        LoHang loHang = null;
        if (lotNumber != null) {
            loHang = loHangRepository.findBySanPhamAndLotNumber(sanPham, lotNumber).orElse(null);
        }
        if (loHang == null) {
            loHang = LoHang.builder()
                    .sanPham(sanPham)
                    .lotNumber(lotNumber != null ? lotNumber : taoMaLo(sanPham))
                    .receivedDate(ngayNhap != null ? ngayNhap : LocalDate.now())
                    .expiryDate(hanSuDung)
                    .quantityRemaining(0)
                    .build();
        }
        int hienCo = loHang.getQuantityRemaining() == null ? 0 : loHang.getQuantityRemaining();
        loHang.setQuantityRemaining(hienCo + soLuong);
        if (hanSuDung != null) {
            loHang.setExpiryDate(hanSuDung);
        }
        loHangRepository.save(loHang);
        capNhatHanSanPham(sanPham);
    }

    public void deductByFEFO(SanPham sanPham, int soLuong) {
        if (soLuong <= 0) {
            return;
        }
        removeExpiredLots(sanPham);
        List<LoHang> loHangs = sapXepLoHangTheoFEFO(sanPham);
        int tongSoLuong = loHangs.stream()
                .mapToInt(lo -> Math.max(0, lo.getQuantityRemaining() == null ? 0 : lo.getQuantityRemaining()))
                .sum();
        if (tongSoLuong < soLuong) {
            throw new NameValueRequiredException("Khong du hang trong cac lo");
        }
        int canTru = soLuong;
        List<LoHang> capNhat = new ArrayList<>();
        for (LoHang loHang : loHangs) {
            if (canTru <= 0) {
                break;
            }
            int ton = loHang.getQuantityRemaining() == null ? 0 : loHang.getQuantityRemaining();
            if (ton <= 0) {
                continue;
            }
            int tru = Math.min(ton, canTru);
            loHang.setQuantityRemaining(ton - tru);
            canTru -= tru;
            capNhat.add(loHang);
        }
        loHangRepository.saveAll(capNhat);
        capNhatHanSanPham(sanPham);
    }

    private List<LoHang> sapXepLoHangTheoFEFO(SanPham sanPham) {
        List<LoHang> loHangs = loHangRepository.findBySanPhamOrderByExpiryDateAsc(sanPham);
        loHangs.sort(Comparator
                .comparing((LoHang lo) -> lo.getExpiryDate() == null ? LocalDate.MAX : lo.getExpiryDate())
                .thenComparing(LoHang::getReceivedDate, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(LoHang::getId, Comparator.nullsLast(Long::compareTo)));
        return loHangs;
    }

    public void capNhatHanSanPham(SanPham sanPham) {
        LocalDateTime expiry = loHangRepository.findBySanPhamOrderByExpiryDateAsc(sanPham).stream()
                .filter(lo -> lo.getQuantityRemaining() != null && lo.getQuantityRemaining() > 0)
                .map(LoHang::getExpiryDate)
                .filter(Objects::nonNull)
                .filter(date -> !date.isBefore(LocalDate.now()))
                .map(LocalDate::atStartOfDay)
                .findFirst()
                .orElse(null);
        sanPham.setExpiryDate(expiry);
    }

    private String buildLotNumber(SanPham sanPham, String input) {
        String sanitized = sanitizeLotNumber(input);
        return sanitized != null ? sanitized : taoMaLo(sanPham);
    }

    private String taoMaLo(SanPham sanPham) {
        String sku = sanPham.getSku() != null ? sanPham.getSku() : "SP";
        return sku + "-" + System.currentTimeMillis();
    }

    private String sanitizeLotNumber(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void removeExpiredLots(SanPham sanPham) {
        List<LoHang> expiredLots = loHangRepository.findBySanPhamOrderByExpiryDateAsc(sanPham).stream()
                .filter(lo -> lo.getExpiryDate() != null && lo.getExpiryDate().isBefore(LocalDate.now()))
                .filter(lo -> lo.getQuantityRemaining() != null && lo.getQuantityRemaining() > 0)
                .toList();
        if (expiredLots.isEmpty()) {
            return;
        }
        int totalRemoved = 0;
        for (LoHang lo : expiredLots) {
            int qty = lo.getQuantityRemaining() == null ? 0 : lo.getQuantityRemaining();
            if (qty > 0) {
                totalRemoved += qty;
                lo.setQuantityRemaining(0);
            }
        }
        if (totalRemoved > 0) {
            int current = sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity();
            sanPham.setStockQuantity(Math.max(0, current - totalRemoved));
        }
        loHangRepository.saveAll(expiredLots);
    }
}
