package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.GiaoDichDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.GiaoDichService;
import com.phegondev.InventoryMgtSystem.services.LoHangQuanLy;
import com.phegondev.InventoryMgtSystem.services.UserService;
import com.phegondev.InventoryMgtSystem.specification.BoLocGiaoDich;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GiaoDichServiceImpl implements GiaoDichService {

    private final GiaoDichRepository giaoDichRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamRepository sanPhamRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final KhoRepository khoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final LoHangQuanLy loHangQuanLy;

    @Override
    @Transactional
    public PhanHoi nhapKho(YeuCauGiaoDich yeuCau) {
        Long sanPhamId = yeuCau.getSanPhamId();
        Long nhaCungCapId = yeuCau.getNhaCungCapId();
        Integer soLuong = yeuCau.getSoLuong();

        if (nhaCungCapId == null) {
            throw new NameValueRequiredException("Yeu cau cung cap nha cung cap");
        }

        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(nhaCungCapId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));
        User nguoiDung = userService.getCurrentLoggedInUser();

        sanPham.setStockQuantity(sanPham.getStockQuantity() + soLuong);
        loHangQuanLy.addNewLot(sanPham, soLuong, yeuCau.getSoLo(), yeuCau.getNgayNhap(), yeuCau.getHanSuDung());
        sanPhamRepository.save(sanPham);

        if (yeuCau.getKhoId() != null) {
            Kho kho = khoRepository.findById(yeuCau.getKhoId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                    .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(kho).quantity(0).build());
            tonKho.setQuantity((tonKho.getQuantity() == null ? 0 : tonKho.getQuantity()) + soLuong);
            tonKhoRepository.save(tonKho);
        }

        GiaoDich giaoDich = GiaoDich.builder()
                .transactionType(LoaiGiaoDich.PURCHASE)
                .status(TrangThaiGiaoDich.COMPLETED)
                .sanPham(sanPham)
                .nhaCungCap(nhaCungCap)
                .user(nguoiDung)
                .totalProducts(soLuong)
                .totalPrice(sanPham.getPrice().multiply(BigDecimal.valueOf(soLuong)))
                .description(yeuCau.getMoTa())
                .note(yeuCau.getGhiChu())
                .build();

        if (yeuCau.getKhachHangId() != null) {
            khachHangRepository.findById(yeuCau.getKhachHangId()).ifPresent(giaoDich::setCustomer);
        }

        giaoDichRepository.save(giaoDich);
        return PhanHoi.builder().status(200).message("Nhap kho thanh cong").build();
    }

    @Override
    @Transactional
    public PhanHoi banHang(YeuCauGiaoDich yeuCau) {
        SanPham sanPham = sanPhamRepository.findById(yeuCau.getSanPhamId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Integer soLuong = yeuCau.getSoLuong();

        if (sanPham.getExpiryDate() != null && sanPham.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new NameValueRequiredException("San pham da het han");
        }
        if (sanPham.getStockQuantity() < soLuong) {
            throw new NameValueRequiredException("Khong du hang trong kho");
        }

        loHangQuanLy.deductByFEFO(sanPham, soLuong);
        sanPham.setStockQuantity(sanPham.getStockQuantity() - soLuong);
        sanPhamRepository.save(sanPham);

        User nguoiDung = userService.getCurrentLoggedInUser();

        GiaoDich giaoDich = GiaoDich.builder()
                .transactionType(LoaiGiaoDich.SALE)
                .status(TrangThaiGiaoDich.COMPLETED)
                .sanPham(sanPham)
                .user(nguoiDung)
                .totalProducts(soLuong)
                .totalPrice(sanPham.getPrice().multiply(BigDecimal.valueOf(soLuong)))
                .description(yeuCau.getMoTa())
                .note(yeuCau.getGhiChu())
                .build();

        if (yeuCau.getKhachHangId() != null) {
            khachHangRepository.findById(yeuCau.getKhachHangId()).ifPresent(giaoDich::setCustomer);
        }

        giaoDichRepository.save(giaoDich);
        return PhanHoi.builder().status(200).message("Ban hang thanh cong").build();
    }

    @Override
    @Transactional
    public PhanHoi traVeNhaCungCap(YeuCauGiaoDich yeuCau) {
        SanPham sanPham = sanPhamRepository.findById(yeuCau.getSanPhamId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(yeuCau.getNhaCungCapId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));
        Integer soLuong = yeuCau.getSoLuong();

        if (sanPham.getStockQuantity() < soLuong) {
            throw new NameValueRequiredException("Khong du hang de tra");
        }

        loHangQuanLy.deductByFEFO(sanPham, soLuong);
        sanPham.setStockQuantity(sanPham.getStockQuantity() - soLuong);
        sanPhamRepository.save(sanPham);

        if (yeuCau.getKhoId() != null) {
            Kho kho = khoRepository.findById(yeuCau.getKhoId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                    .orElseThrow(() -> new NotFoundException("Khong co ton kho tai kho nay"));
            if (tonKho.getQuantity() < soLuong) {
                throw new NameValueRequiredException("Khong du hang tai kho");
            }
            tonKho.setQuantity(tonKho.getQuantity() - soLuong);
            tonKhoRepository.save(tonKho);
        }

        User nguoiDung = userService.getCurrentLoggedInUser();

        GiaoDich giaoDich = GiaoDich.builder()
                .transactionType(LoaiGiaoDich.RETURN_TO_SUPPLIER)
                .status(TrangThaiGiaoDich.PROCESSING)
                .sanPham(sanPham)
                .nhaCungCap(nhaCungCap)
                .user(nguoiDung)
                .totalProducts(soLuong)
                .totalPrice(BigDecimal.ZERO)
                .description(yeuCau.getMoTa())
                .note(yeuCau.getGhiChu())
                .build();

        giaoDichRepository.save(giaoDich);
        return PhanHoi.builder().status(200).message("Dang xu ly tra hang cho nha cung cap").build();
    }

    @Override
    @Transactional
    public PhanHoi khachHangTra(YeuCauGiaoDich yeuCau) {
        SanPham sanPham = sanPhamRepository.findById(yeuCau.getSanPhamId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Integer soLuong = yeuCau.getSoLuong();

        sanPham.setStockQuantity(sanPham.getStockQuantity() + soLuong);
        loHangQuanLy.addBackToLot(sanPham, soLuong, yeuCau.getSoLo(), yeuCau.getNgayNhap(), yeuCau.getHanSuDung());
        sanPhamRepository.save(sanPham);

        if (yeuCau.getKhoId() != null) {
            Kho kho = khoRepository.findById(yeuCau.getKhoId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                    .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(kho).quantity(0).build());
            tonKho.setQuantity((tonKho.getQuantity() == null ? 0 : tonKho.getQuantity()) + soLuong);
            tonKhoRepository.save(tonKho);
        }

        User nguoiDung = userService.getCurrentLoggedInUser();

        GiaoDich giaoDich = GiaoDich.builder()
                .transactionType(LoaiGiaoDich.RETURN_FROM_CUSTOMER)
                .status(TrangThaiGiaoDich.COMPLETED)
                .sanPham(sanPham)
                .user(nguoiDung)
                .totalProducts(soLuong)
                .totalPrice(BigDecimal.ZERO)
                .description(yeuCau.getMoTa())
                .note(yeuCau.getGhiChu())
                .build();

        if (yeuCau.getKhachHangId() != null) {
            khachHangRepository.findById(yeuCau.getKhachHangId()).ifPresent(giaoDich::setCustomer);
        }

        giaoDichRepository.save(giaoDich);
        return PhanHoi.builder().status(200).message("Khach hang tra hang thanh cong").build();
    }

    @Override
    @Transactional
    public PhanHoi chuyenKho(YeuCauGiaoDich yeuCau) {
        Long sanPhamId = yeuCau.getSanPhamId();
        Integer soLuong = yeuCau.getSoLuong();
        Long khoNguonId = yeuCau.getKhoId();
        Long khoDichId = yeuCau.getKhoDichId();

        if (khoNguonId == null || khoDichId == null) {
            throw new NameValueRequiredException("Can nhap kho nguon va kho dich");
        }
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Kho khoNguon = khoRepository.findById(khoNguonId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay kho nguon"));
        Kho khoDich = khoRepository.findById(khoDichId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay kho dich"));

        TonKho tonKhoNguon = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, khoNguon)
                .orElseThrow(() -> new NotFoundException("Khong co ton kho tai kho nguon"));
        if (tonKhoNguon.getQuantity() < soLuong) {
            throw new NameValueRequiredException("Khong du hang tai kho nguon");
        }
        tonKhoNguon.setQuantity(tonKhoNguon.getQuantity() - soLuong);
        tonKhoRepository.save(tonKhoNguon);

        TonKho tonKhoDich = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, khoDich)
                .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(khoDich).quantity(0).build());
        tonKhoDich.setQuantity((tonKhoDich.getQuantity() == null ? 0 : tonKhoDich.getQuantity()) + soLuong);
        tonKhoRepository.save(tonKhoDich);

        return PhanHoi.builder().status(200).message("Chuyen kho thanh cong").build();
    }

    @Override
    public PhanHoi layTatCaGiaoDich(int page, int size, String filter) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Specification<GiaoDich> spec = BoLocGiaoDich.byFilter(filter);
        Page<GiaoDich> pageKetQua = giaoDichRepository.findAll(spec, pageable);

        List<GiaoDichDTO> giaoDichDTOS = modelMapper.map(pageKetQua.getContent(), new TypeToken<List<GiaoDichDTO>>() {
        }.getType());

        giaoDichDTOS.forEach(dto -> {
            dto.setUser(null);
            dto.setSanPham(null);
            dto.setNhaCungCap(null);
        });

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .giaoDichs(giaoDichDTOS)
                .totalElements(pageKetQua.getTotalElements())
                .totalPages(pageKetQua.getTotalPages())
                .build();
    }

    @Override
    public PhanHoi layGiaoDichTheoId(Long id) {
        GiaoDich giaoDich = giaoDichRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay giao dich"));
        GiaoDichDTO dto = modelMapper.map(giaoDich, GiaoDichDTO.class);
        if (dto.getUser() != null) {
            dto.getUser().setGiaoDichs(null);
        }
        return PhanHoi.builder().status(200).message("Thanh cong").giaoDich(dto).build();
    }

    @Override
    public PhanHoi layGiaoDichTheoThangNam(int month, int year) {
        List<GiaoDich> danhSach = giaoDichRepository.findAll(BoLocGiaoDich.byMonthAndYear(month, year));
        List<GiaoDichDTO> giaoDichDTOS = modelMapper.map(danhSach, new TypeToken<List<GiaoDichDTO>>() {
        }.getType());
        giaoDichDTOS.forEach(dto -> {
            dto.setUser(null);
            dto.setSanPham(null);
            dto.setNhaCungCap(null);
        });
        return PhanHoi.builder().status(200).message("Thanh cong").giaoDichs(giaoDichDTOS).build();
    }

    @Override
    public PhanHoi capNhatTrangThai(Long giaoDichId, TrangThaiGiaoDich trangThai) {
        GiaoDich giaoDich = giaoDichRepository.findById(giaoDichId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay giao dich"));
        giaoDich.setStatus(trangThai);
        giaoDich.setUpdateAt(LocalDateTime.now());
        giaoDichRepository.save(giaoDich);
        return PhanHoi.builder().status(200).message("Cap nhat trang thai thanh cong").build();
    }

}




