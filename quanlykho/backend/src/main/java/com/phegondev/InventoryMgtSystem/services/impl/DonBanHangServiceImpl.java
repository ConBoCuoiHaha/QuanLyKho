package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.*;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiChiTietDonBan;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonBanHang;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.DonBanHangService;
import com.phegondev.InventoryMgtSystem.services.GiaoDichService;
import com.phegondev.InventoryMgtSystem.services.NhatKyThayDoiService;
import com.phegondev.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonBanHangServiceImpl implements DonBanHangService {

    private final DonBanHangRepository donBanHangRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final KhoRepository khoRepository;
    private final UserService userService;
    private final GiaoDichService giaoDichService;
    private final ModelMapper modelMapper;
    private final NhatKyThayDoiService nhatKyThayDoiService;

    private static final Map<TrangThaiDonBanHang, List<TrangThaiDonBanHang>> TRANSITIONS = Map.of(
            TrangThaiDonBanHang.NHAP, List.of(TrangThaiDonBanHang.XAC_NHAN, TrangThaiDonBanHang.DA_HUY),
            TrangThaiDonBanHang.XAC_NHAN, List.of(TrangThaiDonBanHang.DONG_GOI, TrangThaiDonBanHang.DA_HUY),
            TrangThaiDonBanHang.DONG_GOI, List.of(TrangThaiDonBanHang.GIAO_HANG, TrangThaiDonBanHang.DA_HUY),
            TrangThaiDonBanHang.GIAO_HANG, List.of(TrangThaiDonBanHang.HOAN_THANH, TrangThaiDonBanHang.DA_HUY),
            TrangThaiDonBanHang.HOAN_THANH, List.of(),
            TrangThaiDonBanHang.DA_HUY, List.of()
    );

    @Override
    @Transactional
    public PhanHoi taoDon(DonBanHangYeuCau yeuCau) {
        if (yeuCau.getItems() == null || yeuCau.getItems().isEmpty()) {
            throw new NameValueRequiredException("Can cung cap it nhat mot san pham");
        }
        KhachHang khachHang = khachHangRepository.findById(yeuCau.getKhachHangId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay khach hang"));
        Kho kho = null;
        if (yeuCau.getKhoId() != null) {
            kho = khoRepository.findById(yeuCau.getKhoId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
        }
        User nguoiTao = userService.getCurrentLoggedInUser();

        DonBanHang order = new DonBanHang();
        order.setMaDon(generateMa());
        order.setTrangThai(TrangThaiDonBanHang.NHAP);
        order.setNgayGiaoDuKien(yeuCau.getNgayGiaoDuKien());
        order.setGhiChu(yeuCau.getGhiChu());
        order.setKhachHang(khachHang);
        order.setKho(kho);
        order.setNguoiTao(nguoiTao);
        order.setTongSoLuongDaGiao(0);

        for (DonBanHangYeuCau.ItemYeuCau itemYeuCau : yeuCau.getItems()) {
            SanPham sanPham = sanPhamRepository.findById(itemYeuCau.getSanPhamId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
            int available = getSafeQuantity(sanPham.getStockQuantity()) - getSafeQuantity(sanPham.getReservedQuantity());
            if (itemYeuCau.getSoLuong() > available) {
                throw new NameValueRequiredException("San pham " + sanPham.getName() + " khong du so luong. Con lai: " + available);
            }
            increaseReserved(sanPham, itemYeuCau.getSoLuong());

            DonBanHangChiTiet chiTiet = DonBanHangChiTiet.builder()
                    .donBanHang(order)
                    .sanPham(sanPham)
                    .soLuong(itemYeuCau.getSoLuong())
                    .soLuongDaGiao(0)
                    .donGia(resolveDonGia(itemYeuCau.getDonGia(), sanPham.getPrice()))
                    .trangThai(TrangThaiChiTietDonBan.CHO_XU_LY)
                    .build();
            order.getChiTiets().add(chiTiet);
        }

        refreshTotals(order);
        DonBanHang saved = donBanHangRepository.save(order);
        return PhanHoi.builder()
                .status(200)
                .message("Tao don ban hang thanh cong")
                .donBanHang(mapToDto(saved, true))
                .build();
    }

    @Override
    @Transactional
    public PhanHoi capNhatTrangThai(Long id, DonBanHangTrangThaiYeuCau yeuCau) {
        DonBanHang order = getOrderWithDetails(id);
        TrangThaiDonBanHang trangThaiCu = order.getTrangThai();
        DonBanHangDTO duLieuCu = mapToDto(order, true);
        TrangThaiDonBanHang targetStatus = yeuCau.getTrangThai();
        if (!isTransitionAllowed(trangThaiCu, targetStatus)) {
            throw new NameValueRequiredException("Trang thai khong hop le");
        }
        if (targetStatus == TrangThaiDonBanHang.DA_HUY) {
            releaseReserved(order);
        }
        if (targetStatus == TrangThaiDonBanHang.HOAN_THANH) {
            thucHienBanHang(order);
        }
        order.setTrangThai(targetStatus);
        DonBanHang saved = donBanHangRepository.save(order);
        DonBanHangDTO response = mapToDto(saved, true);
        ghiNhatKy("CAP_NHAT_TRANG_THAI", "Cap nhat trang thai don ban " + saved.getMaDon() + " tu " + trangThaiCu + " sang " + targetStatus,
                duLieuCu, response, saved.getId());
        return PhanHoi.builder()
                .status(200)
                .message("Cap nhat trang thai thanh cong")
                .donBanHang(response)
                .build();
    }

    @Override
    public PhanHoi danhSach(int page, int size, String trangThai, Long khachHangId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<DonBanHang> spec = (root, query, cb) -> cb.conjunction();

        if (trangThai != null && !trangThai.isBlank()) {
            TrangThaiDonBanHang parsed = parseTrangThai(trangThai);
            Specification<DonBanHang> statusSpec = (root, query, cb) -> cb.equal(root.get("trangThai"), parsed);
            spec = spec.and(statusSpec);
        }

        if (khachHangId != null) {
            Specification<DonBanHang> customerSpec = (root, query, cb) -> cb.equal(root.get("khachHang").get("id"), khachHangId);
            spec = spec.and(customerSpec);
        }

        Page<DonBanHang> pageData = donBanHangRepository.findAll(spec, pageable);
        List<DonBanHangDTO> dtos = pageData.getContent().stream()
                .map(order -> mapToDto(order, false))
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .donBanHangs(dtos)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .build();
    }

    @Override
    public PhanHoi chiTiet(Long id) {
        DonBanHang order = donBanHangRepository.findWithChiTietsById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay don ban hang"));
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .donBanHang(mapToDto(order, true))
                .build();
    }

    private DonBanHangDTO mapToDto(DonBanHang order, boolean includeDetails) {
        KhachHangDTO khachHangDTO = order.getKhachHang() != null
                ? modelMapper.map(order.getKhachHang(), KhachHangDTO.class)
                : null;
        UserDTO userDTO = order.getNguoiTao() != null
                ? modelMapper.map(order.getNguoiTao(), UserDTO.class)
                : null;
        KhoDTO khoDTO = order.getKho() != null
                ? modelMapper.map(order.getKho(), KhoDTO.class)
                : null;

        List<DonBanHangChiTietDTO> detailDtos = null;
        if (includeDetails && order.getChiTiets() != null) {
            detailDtos = order.getChiTiets().stream()
                    .map(this::mapDetail)
                    .toList();
        }

        return DonBanHangDTO.builder()
                .id(order.getId())
                .maDon(order.getMaDon())
                .trangThai(order.getTrangThai())
                .ngayGiaoDuKien(order.getNgayGiaoDuKien())
                .ghiChu(order.getGhiChu())
                .tongSoLuong(order.getTongSoLuong())
                .tongSoLuongDaGiao(order.getTongSoLuongDaGiao())
                .tongTien(order.getTongTien())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .khachHang(khachHangDTO)
                .nguoiTao(userDTO)
                .kho(khoDTO)
                .chiTiets(detailDtos)
                .build();
    }

    private DonBanHangChiTietDTO mapDetail(DonBanHangChiTiet chiTiet) {
        SanPhamDTO sanPhamDTO = modelMapper.map(chiTiet.getSanPham(), SanPhamDTO.class);
        return DonBanHangChiTietDTO.builder()
                .id(chiTiet.getId())
                .sanPham(sanPhamDTO)
                .soLuong(chiTiet.getSoLuong())
                .soLuongDaGiao(chiTiet.getSoLuongDaGiao())
                .donGia(chiTiet.getDonGia())
                .trangThai(chiTiet.getTrangThai())
                .build();
    }

    private DonBanHang getOrderWithDetails(Long id) {
        return donBanHangRepository.findWithChiTietsById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay don ban hang"));
    }

    private boolean isTransitionAllowed(TrangThaiDonBanHang current, TrangThaiDonBanHang target) {
        List<TrangThaiDonBanHang> allowed = TRANSITIONS.getOrDefault(current, Collections.emptyList());
        return allowed.contains(target);
    }

    private TrangThaiDonBanHang parseTrangThai(String value) {
        try {
            return TrangThaiDonBanHang.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new NameValueRequiredException("Trang thai khong hop le");
        }
    }

    private void increaseReserved(SanPham sanPham, int quantity) {
        int reserved = getSafeQuantity(sanPham.getReservedQuantity()) + quantity;
        sanPham.setReservedQuantity(reserved);
        sanPhamRepository.save(sanPham);
    }

    private void decreaseReserved(SanPham sanPham, int quantity) {
        int reserved = Math.max(0, getSafeQuantity(sanPham.getReservedQuantity()) - quantity);
        sanPham.setReservedQuantity(reserved);
        sanPhamRepository.save(sanPham);
    }

    private int getSafeQuantity(Integer value) {
        return value == null ? 0 : value;
    }

    private void releaseReserved(DonBanHang order) {
        if (order.getChiTiets() == null) {
            return;
        }
        for (DonBanHangChiTiet chiTiet : order.getChiTiets()) {
            int remaining = chiTiet.getSoLuong() - chiTiet.getSoLuongDaGiao();
            if (remaining > 0) {
                decreaseReserved(chiTiet.getSanPham(), remaining);
            }
            chiTiet.setTrangThai(TrangThaiChiTietDonBan.DA_HUY);
        }
        order.setTongSoLuongDaGiao(0);
    }

    private void thucHienBanHang(DonBanHang order) {
        if (order.getChiTiets() == null || order.getChiTiets().isEmpty()) {
            throw new NameValueRequiredException("Don ban hang khong co san pham");
        }

        Map<Long, SanPham> sanPhamCache = order.getChiTiets().stream()
                .map(DonBanHangChiTiet::getSanPham)
                .collect(Collectors.toMap(SanPham::getId, sp -> sp, (sp1, sp2) -> sp1));

        for (DonBanHangChiTiet chiTiet : order.getChiTiets()) {
            int canhBao = chiTiet.getSoLuong() - getSafeQuantity(chiTiet.getSoLuongDaGiao());
            if (canhBao <= 0) {
                continue;
            }
            YeuCauGiaoDich yeuCau = new YeuCauGiaoDich();
            yeuCau.setSanPhamId(chiTiet.getSanPham().getId());
            yeuCau.setSoLuong(canhBao);
            yeuCau.setKhachHangId(order.getKhachHang() != null ? order.getKhachHang().getId() : null);
            yeuCau.setKhoId(order.getKho() != null ? order.getKho().getId() : null);
            yeuCau.setMoTa("Ban hang tu SO " + order.getMaDon());
            yeuCau.setGhiChu(order.getGhiChu());

            giaoDichService.banHang(yeuCau);
            chiTiet.setSoLuongDaGiao(chiTiet.getSoLuong());
            chiTiet.setTrangThai(TrangThaiChiTietDonBan.HOAN_THANH);

            SanPham sanPham = sanPhamCache.get(chiTiet.getSanPham().getId());
            decreaseReserved(sanPham, chiTiet.getSoLuong());
        }

        refreshTotals(order);
    }

    private void refreshTotals(DonBanHang order) {
        int tongSoLuong = order.getChiTiets().stream()
                .mapToInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())
                .sum();
        int tongSoLuongDaGiao = order.getChiTiets().stream()
                .mapToInt(ct -> ct.getSoLuongDaGiao() == null ? 0 : ct.getSoLuongDaGiao())
                .sum();
        BigDecimal tongTien = order.getChiTiets().stream()
                .map(ct -> {
                    BigDecimal gia = ct.getDonGia() == null ? BigDecimal.ZERO : ct.getDonGia();
                    int sl = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
                    return gia.multiply(BigDecimal.valueOf(sl));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTongSoLuong(tongSoLuong);
        order.setTongSoLuongDaGiao(tongSoLuongDaGiao);
        order.setTongTien(tongTien);
    }

    private String generateMa() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String random = String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
        return "SO-" + timestamp + random;
    }

    private BigDecimal resolveDonGia(BigDecimal requestValue, BigDecimal fallback) {
        if (requestValue != null) {
            return requestValue;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }
    private void ghiNhatKy(String action, String message, Object oldData, Object newData, Long targetId) {
        nhatKyThayDoiService.ghi("DonBanHang", action, message, "DonBanHang", targetId, oldData, newData);
    }
}




