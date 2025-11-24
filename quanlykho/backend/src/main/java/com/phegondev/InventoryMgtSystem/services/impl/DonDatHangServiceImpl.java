package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.*;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiChiTietDonDat;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiDonDatHang;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.DonDatHangRepository;
import com.phegondev.InventoryMgtSystem.repositories.NhaCungCapRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import com.phegondev.InventoryMgtSystem.services.DonDatHangService;
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
public class DonDatHangServiceImpl implements DonDatHangService {

    private final DonDatHangRepository donDatHangRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final SanPhamRepository sanPhamRepository;
    private final UserService userService;
    private final NhatKyThayDoiService nhatKyThayDoiService;
    private final GiaoDichService giaoDichService;
    private final ModelMapper modelMapper;

    private static final Map<TrangThaiDonDatHang, List<TrangThaiDonDatHang>> TRANSITIONS = Map.of(
            TrangThaiDonDatHang.NHAP, List.of(TrangThaiDonDatHang.CHO_DUYET, TrangThaiDonDatHang.DA_DUYET, TrangThaiDonDatHang.DA_HUY),
            TrangThaiDonDatHang.CHO_DUYET, List.of(TrangThaiDonDatHang.DA_DUYET, TrangThaiDonDatHang.DA_HUY),
            TrangThaiDonDatHang.DA_DUYET, List.of(TrangThaiDonDatHang.DANG_GIAO, TrangThaiDonDatHang.DA_HUY),
            TrangThaiDonDatHang.DANG_GIAO, List.of(TrangThaiDonDatHang.HOAN_THANH, TrangThaiDonDatHang.DA_HUY),
            TrangThaiDonDatHang.HOAN_THANH, List.of(),
            TrangThaiDonDatHang.DA_HUY, List.of()
    );

    @Override
    @Transactional
    public PhanHoi taoDon(DonDatHangYeuCau yeuCau) {
        if (yeuCau.getItems() == null || yeuCau.getItems().isEmpty()) {
            throw new NameValueRequiredException("Can cung cap it nhat mot san pham");
        }
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(yeuCau.getNhaCungCapId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));
        User nguoiTao = userService.getCurrentLoggedInUser();

        DonDatHang order = new DonDatHang();
        order.setMaDon(generateMa());
        order.setTrangThai(TrangThaiDonDatHang.NHAP);
        order.setNgayDuKien(yeuCau.getNgayDuKien());
        order.setGhiChu(yeuCau.getGhiChu());
        order.setNhaCungCap(nhaCungCap);
        order.setNguoiTao(nguoiTao);
        order.setTongSoLuongDaNhan(0);

        for (DonDatHangYeuCau.ItemYeuCau itemYeuCau : yeuCau.getItems()) {
            SanPham sanPham = sanPhamRepository.findById(itemYeuCau.getSanPhamId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
            DonDatHangChiTiet chiTiet = DonDatHangChiTiet.builder()
                    .donDatHang(order)
                    .sanPham(sanPham)
                    .soLuong(itemYeuCau.getSoLuong())
                    .soLuongDaNhan(0)
                    .donGia(resolveDonGia(itemYeuCau.getDonGia(), sanPham.getPrice()))
                    .trangThai(TrangThaiChiTietDonDat.CHO_NHAN)
                    .build();
            order.getChiTiets().add(chiTiet);
        }

        refreshTotals(order);
        DonDatHang saved = donDatHangRepository.save(order);
        DonDatHangDTO response = mapToDto(saved, true);
        ghiNhatKy("TAO", "Tao don dat hang " + saved.getMaDon(), null, response, saved.getId());
        return PhanHoi.builder()
                .status(200)
                .message("Tao don dat hang thanh cong")
                .donDatHang(response)
                .build();
    }

    @Override
    @Transactional
    public PhanHoi capNhatTrangThai(Long id, DonDatHangTrangThaiYeuCau yeuCau) {
        DonDatHang order = getOrderWithDetails(id);
        TrangThaiDonDatHang trangThaiCu = order.getTrangThai();
        DonDatHangDTO duLieuCu = mapToDto(order, true);
        TrangThaiDonDatHang trangThai = yeuCau.getTrangThai();
        if (!isTransitionAllowed(trangThaiCu, trangThai)) {
            throw new NameValueRequiredException("Trang thai khong hop le");
        }
        if (trangThai == TrangThaiDonDatHang.HOAN_THANH &&
                order.getChiTiets().stream().anyMatch(ct -> !Objects.equals(ct.getSoLuong(), ct.getSoLuongDaNhan()))) {
            throw new NameValueRequiredException("Chua nhan du hang, khong the chot hoan thanh");
        }
        if (trangThai == TrangThaiDonDatHang.DA_HUY) {
            releaseReserved(order);
        }
        order.setTrangThai(trangThai);
        DonDatHang saved = donDatHangRepository.save(order);
        DonDatHangDTO response = mapToDto(saved, true);
        ghiNhatKy("CAP_NHAT_TRANG_THAI", "Cap nhat trang thai don " + saved.getMaDon() + " tu " + trangThaiCu + " sang " + trangThai,
                duLieuCu, response, saved.getId());
        return PhanHoi.builder()
                .status(200)
                .message("Cap nhat trang thai thanh cong")
                .donDatHang(response)
                .build();
    }

    @Override
    public PhanHoi danhSach(int page, int size, String trangThai, Long nhaCungCapId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<DonDatHang> spec = (root, query, cb) -> cb.conjunction();

        if (trangThai != null && !trangThai.isBlank()) {
            TrangThaiDonDatHang parsed = parseTrangThai(trangThai);
            Specification<DonDatHang> statusSpec = (root, query, cb) -> cb.equal(root.get("trangThai"), parsed);
            spec = spec.and(statusSpec);
        }

        if (nhaCungCapId != null) {
            Specification<DonDatHang> supplierSpec = (root, query, cb) -> cb.equal(root.get("nhaCungCap").get("id"), nhaCungCapId);
            spec = spec.and(supplierSpec);
        }

        Page<DonDatHang> pageData = donDatHangRepository.findAll(spec, pageable);
        List<DonDatHangDTO> dtos = pageData.getContent().stream()
                .map(order -> mapToDto(order, false))
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .donDatHangs(dtos)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .build();
    }

    @Override
    public PhanHoi chiTiet(Long id) {
        DonDatHang order = donDatHangRepository.findWithChiTietsById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay don dat hang"));
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .donDatHang(mapToDto(order, true))
                .build();
    }

    @Override
    @Transactional
    public PhanHoi nhanHang(Long id, NhanHangYeuCau yeuCau) {
        DonDatHang order = getOrderWithDetails(id);
        DonDatHangDTO duLieuCu = mapToDto(order, true);
        if (order.getTrangThai() == TrangThaiDonDatHang.NHAP) {
            throw new NameValueRequiredException("Don dat hang chua duoc duyet");
        }
        if (order.getTrangThai() == TrangThaiDonDatHang.DA_HUY) {
            throw new NameValueRequiredException("Don dat hang da bi huy");
        }
        if (yeuCau.getItems() == null || yeuCau.getItems().isEmpty()) {
            throw new NameValueRequiredException("Chua co chi tiet de nhan");
        }
        Map<Long, DonDatHangChiTiet> detailMap = order.getChiTiets().stream()
                .collect(Collectors.toMap(DonDatHangChiTiet::getId, ct -> ct));

        for (NhanHangYeuCau.ChiTietNhanYeuCau item : yeuCau.getItems()) {
            DonDatHangChiTiet chiTiet = detailMap.get(item.getChiTietId());
            if (chiTiet == null) {
                throw new NotFoundException("Chi tiet don khong hop le");
            }
            int remaining = chiTiet.getSoLuong() - chiTiet.getSoLuongDaNhan();
            if (item.getSoLuongNhan() > remaining) {
                throw new NameValueRequiredException("So luong nhan vuot qua so luong con lai");
            }

            YeuCauGiaoDich giaoDichYeuCau = new YeuCauGiaoDich();
            giaoDichYeuCau.setSanPhamId(chiTiet.getSanPham().getId());
            giaoDichYeuCau.setSoLuong(item.getSoLuongNhan());
            giaoDichYeuCau.setNhaCungCapId(order.getNhaCungCap().getId());
            giaoDichYeuCau.setKhoId(item.getKhoId());
            giaoDichYeuCau.setSoLo(item.getSoLo());
            giaoDichYeuCau.setNgayNhap(item.getNgayNhap());
            giaoDichYeuCau.setHanSuDung(item.getHanSuDung());
            giaoDichYeuCau.setMoTa("Nhan hang tu PO " + order.getMaDon());
            giaoDichYeuCau.setGhiChu(yeuCau.getGhiChu());

            giaoDichService.nhapKho(giaoDichYeuCau);

            chiTiet.setSoLuongDaNhan(chiTiet.getSoLuongDaNhan() + item.getSoLuongNhan());
            if (Objects.equals(chiTiet.getSoLuong(), chiTiet.getSoLuongDaNhan())) {
                chiTiet.setTrangThai(TrangThaiChiTietDonDat.HOAN_THANH);
            } else {
                chiTiet.setTrangThai(TrangThaiChiTietDonDat.DANG_NHAN);
            }
        }

        refreshTotals(order);
        if (order.getChiTiets().stream().allMatch(ct -> Objects.equals(ct.getSoLuong(), ct.getSoLuongDaNhan()))) {
            order.setTrangThai(TrangThaiDonDatHang.HOAN_THANH);
        } else if (order.getTrangThai() == TrangThaiDonDatHang.CHO_DUYET || order.getTrangThai() == TrangThaiDonDatHang.DA_DUYET) {
            order.setTrangThai(TrangThaiDonDatHang.DANG_GIAO);
        }
        DonDatHang saved = donDatHangRepository.save(order);
        DonDatHangDTO response = mapToDto(saved, true);
        ghiNhatKy("CAP_NHAT_NHAN_HANG", "Cap nhat nhan hang don " + saved.getMaDon(), duLieuCu, response, saved.getId());
        return PhanHoi.builder()
                .status(200)
                .message("Cap nhat nhan hang thanh cong")
                .donDatHang(response)
                .build();
    }

    private DonDatHangDTO mapToDto(DonDatHang order, boolean includeDetails) {
        NhaCungCapDTO nhaCungCapDTO = order.getNhaCungCap() != null
                ? modelMapper.map(order.getNhaCungCap(), NhaCungCapDTO.class)
                : null;
        UserDTO userDTO = order.getNguoiTao() != null
                ? modelMapper.map(order.getNguoiTao(), UserDTO.class)
                : null;

        List<DonDatHangChiTietDTO> chiTietDTOS = null;
        if (includeDetails && order.getChiTiets() != null) {
            chiTietDTOS = order.getChiTiets().stream()
                    .map(this::mapDetail)
                    .toList();
        }

        return DonDatHangDTO.builder()
                .id(order.getId())
                .maDon(order.getMaDon())
                .trangThai(order.getTrangThai())
                .ngayDuKien(order.getNgayDuKien())
                .ghiChu(order.getGhiChu())
                .tongSoLuong(order.getTongSoLuong())
                .tongSoLuongDaNhan(order.getTongSoLuongDaNhan())
                .tongTien(order.getTongTien())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .nhaCungCap(nhaCungCapDTO)
                .nguoiTao(userDTO)
                .chiTiets(chiTietDTOS)
                .build();
    }

    private DonDatHangChiTietDTO mapDetail(DonDatHangChiTiet chiTiet) {
        SanPhamDTO sanPhamDTO = modelMapper.map(chiTiet.getSanPham(), SanPhamDTO.class);
        return DonDatHangChiTietDTO.builder()
                .id(chiTiet.getId())
                .sanPham(sanPhamDTO)
                .soLuong(chiTiet.getSoLuong())
                .soLuongDaNhan(chiTiet.getSoLuongDaNhan())
                .donGia(chiTiet.getDonGia())
                .trangThai(chiTiet.getTrangThai())
                .build();
    }

    private DonDatHang getOrderWithDetails(Long id) {
        return donDatHangRepository.findWithChiTietsById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay don dat hang"));
    }

    private boolean isTransitionAllowed(TrangThaiDonDatHang current, TrangThaiDonDatHang target) {
        List<TrangThaiDonDatHang> allowed = TRANSITIONS.getOrDefault(current, Collections.emptyList());
        return allowed.contains(target) || current == target;
    }

    private void releaseReserved(DonDatHang order) {
        if (order.getChiTiets() == null) {
            return;
        }
        for (DonDatHangChiTiet chiTiet : order.getChiTiets()) {
            if (!Objects.equals(chiTiet.getSoLuong(), chiTiet.getSoLuongDaNhan())) {
                chiTiet.setTrangThai(TrangThaiChiTietDonDat.CHO_NHAN);
            }
        }
    }

    private TrangThaiDonDatHang parseTrangThai(String value) {
        try {
            return TrangThaiDonDatHang.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new NameValueRequiredException("Trang thai khong hop le");
        }
    }

    private void refreshTotals(DonDatHang order) {
        int tongSoLuong = order.getChiTiets().stream()
                .mapToInt(ct -> ct.getSoLuong() == null ? 0 : ct.getSoLuong())
                .sum();
        int tongSoLuongDaNhan = order.getChiTiets().stream()
                .mapToInt(ct -> ct.getSoLuongDaNhan() == null ? 0 : ct.getSoLuongDaNhan())
                .sum();
        BigDecimal tongTien = order.getChiTiets().stream()
                .map(ct -> {
                    BigDecimal gia = ct.getDonGia() == null ? BigDecimal.ZERO : ct.getDonGia();
                    int sl = ct.getSoLuong() == null ? 0 : ct.getSoLuong();
                    return gia.multiply(BigDecimal.valueOf(sl));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTongSoLuong(tongSoLuong);
        order.setTongSoLuongDaNhan(tongSoLuongDaNhan);
        order.setTongTien(tongTien);
    }

    private String generateMa() {
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(LocalDateTime.now());
        String random = String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
        return "PO-" + timestamp + random;
    }

    private BigDecimal resolveDonGia(BigDecimal requestValue, BigDecimal fallback) {
        if (requestValue != null) {
            return requestValue;
        }
        return fallback != null ? fallback : BigDecimal.ZERO;
    }
    private void ghiNhatKy(String action, String message, Object oldData, Object newData, Long targetId) {
        nhatKyThayDoiService.ghi("DonDatHang", action, message, "DonDatHang", targetId, oldData, newData);
    }
}




