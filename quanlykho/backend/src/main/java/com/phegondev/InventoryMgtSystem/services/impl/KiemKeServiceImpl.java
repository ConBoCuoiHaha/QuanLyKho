package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.KiemKeDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.SanPhamDTO;
import com.phegondev.InventoryMgtSystem.dtos.YeuCauKiemKe;
import com.phegondev.InventoryMgtSystem.exceptions.NameValueRequiredException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.KiemKeService;
import com.phegondev.InventoryMgtSystem.services.LoHangQuanLy;
import com.phegondev.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KiemKeServiceImpl implements KiemKeService {

    private final SanPhamRepository sanPhamRepository;
    private final KhoRepository khoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final KiemKeRepository kiemKeRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final LoHangQuanLy loHangQuanLy;

    @Override
    @Transactional
    public PhanHoi thucHienKiemKe(YeuCauKiemKe yeuCau) {
        SanPham sanPham = sanPhamRepository.findById(yeuCau.getSanPhamId())
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Kho kho = null;
        if (yeuCau.getKhoId() != null) {
            kho = khoRepository.findById(yeuCau.getKhoId())
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
        }

        int heThong = laySoLuongHeThong(sanPham, kho);
        int thucTe = Objects.requireNonNullElse(yeuCau.getSoLuongThucTe(), 0);
        if (thucTe < 0) {
            throw new NameValueRequiredException("So luong thuc te phai >= 0");
        }
        int chenhlech = thucTe - heThong;

        if (chenhlech > 0) {
            tangSoLuong(sanPham, kho, chenhlech);
        } else if (chenhlech < 0) {
            giamSoLuong(sanPham, kho, Math.abs(chenhlech));
        }

        final Kho khoThucTe = kho;
        if (khoThucTe != null) {
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, khoThucTe)
                    .orElseGet(() -> TonKho.builder().sanPham(sanPham).warehouse(khoThucTe).quantity(0).build());
            tonKho.setQuantity(thucTe);
            tonKhoRepository.save(tonKho);
        }

        sanPhamRepository.save(sanPham);
        User nguoiDung = userService.getCurrentLoggedInUser();

        KiemKeKho record = KiemKeKho.builder()
                .sanPham(sanPham)
                .kho(kho)
                .soLuongHeThong(heThong)
                .soLuongThucTe(thucTe)
                .chenhlech(chenhlech)
                .lyDo(yeuCau.getLyDo())
                .ghiChu(yeuCau.getGhiChu())
                .nguoiDung(nguoiDung)
                .build();
        kiemKeRepository.save(record);

        KiemKeDTO dto = mapToDTO(record);

        return PhanHoi.builder()
                .status(200)
                .message("Kiem ke thanh cong")
                .kiemKe(dto)
                .build();
    }

    @Override
    public PhanHoi layLichSuKiemKe(Long sanPhamId, Long khoId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<KiemKeKho> ketQua;
        if (sanPhamId != null && khoId != null) {
            ketQua = kiemKeRepository.findBySanPhamIdAndKhoId(sanPhamId, khoId, pageable);
        } else if (sanPhamId != null) {
            ketQua = kiemKeRepository.findBySanPhamId(sanPhamId, pageable);
        } else if (khoId != null) {
            ketQua = kiemKeRepository.findByKhoId(khoId, pageable);
        } else {
            ketQua = kiemKeRepository.findAll(pageable);
        }

        List<KiemKeDTO> dtoList = ketQua.getContent().stream()
                .map(this::mapToDTO)
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .kiemKes(dtoList)
                .totalElements(ketQua.getTotalElements())
                .totalPages(ketQua.getTotalPages())
                .build();
    }

    @Override
    public PhanHoi hienTrangTonKho(Long sanPhamId, Long khoId) {
        if (sanPhamId == null) {
            throw new NameValueRequiredException("San pham la bat buoc");
        }
        SanPham sanPham = sanPhamRepository.findById(sanPhamId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham"));
        Kho kho = null;
        if (khoId != null) {
            kho = khoRepository.findById(khoId)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay kho"));
        }
        int heThong = laySoLuongHeThong(sanPham, kho);
        SanPhamDTO sanPhamDTO = modelMapper.map(sanPham, SanPhamDTO.class);

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .sanPham(sanPhamDTO)
                .soLuongHienTai(heThong)
                .build();
    }

    private int laySoLuongHeThong(SanPham sanPham, Kho kho) {
        if (kho == null) {
            return sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity();
        }
        return tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                .map(TonKho::getQuantity)
                .orElse(0);
    }

    private void tangSoLuong(SanPham sanPham, Kho kho, int chenhlech) {
        sanPham.setStockQuantity((sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity()) + chenhlech);
        loHangQuanLy.addNewLot(sanPham, chenhlech, taoSoLoKiemKe(sanPham), LocalDate.now(), null);
    }

    private void giamSoLuong(SanPham sanPham, Kho kho, int soLuongGiam) {
        int hienCo = sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity();
        if (hienCo < soLuongGiam) {
            throw new NameValueRequiredException("Khong du hang de dieu chinh");
        }
        loHangQuanLy.deductByFEFO(sanPham, soLuongGiam);
        sanPham.setStockQuantity(hienCo - soLuongGiam);
        if (kho != null) {
            TonKho tonKho = tonKhoRepository.findBySanPhamAndWarehouse(sanPham, kho)
                    .orElseThrow(() -> new NotFoundException("Khong tim thay ton kho tai kho"));
            if (tonKho.getQuantity() < soLuongGiam) {
                throw new NameValueRequiredException("Khong du hang tai kho");
            }
            tonKho.setQuantity(tonKho.getQuantity() - soLuongGiam);
            tonKhoRepository.save(tonKho);
        }
    }

    private String taoSoLoKiemKe(SanPham sanPham) {
        return "KK-" + (sanPham.getSku() != null ? sanPham.getSku() : "SP") + "-" + System.currentTimeMillis();
    }

    private KiemKeDTO mapToDTO(KiemKeKho record) {
        KiemKeDTO dto = modelMapper.map(record, KiemKeDTO.class);
        if (record.getSanPham() != null) {
            dto.setSanPham(modelMapper.map(record.getSanPham(), SanPhamDTO.class));
        }
        if (record.getKho() != null) {
            dto.setKhoId(record.getKho().getId());
            dto.setTenKho(record.getKho().getName());
        }
        if (record.getNguoiDung() != null) {
            dto.setNguoiThucHien(record.getNguoiDung().getName());
        }
        return dto;
    }
}
