package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.GiaoDichDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhachHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.models.KhachHang;
import com.phegondev.InventoryMgtSystem.repositories.GiaoDichRepository;
import com.phegondev.InventoryMgtSystem.repositories.KhachHangRepository;
import com.phegondev.InventoryMgtSystem.repositories.view.ThongKeKhachHangView;
import com.phegondev.InventoryMgtSystem.services.KhachHangService;
import com.phegondev.InventoryMgtSystem.services.NhatKyThayDoiService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KhachHangServiceImpl implements KhachHangService {

    private final KhachHangRepository khachHangRepository;
    private final GiaoDichRepository giaoDichRepository;
    private final ModelMapper modelMapper;
    private final NhatKyThayDoiService nhatKyThayDoiService;

    @Override
    public PhanHoi add(KhachHangDTO dto) {
        KhachHang entity = KhachHang.builder()
                .name(dto.getName())
                .phone(dto.getPhone())
                .email(dto.getEmail())
                .address(dto.getAddress())
                .notes(dto.getNotes())
                .build();
        khachHangRepository.save(entity);
        KhachHangDTO response = mapToDto(entity, null);
        ghiNhatKy("TAO", "Them khach hang " + entity.getName(), null, response, entity.getId());
        return PhanHoi.builder()
                .status(200)
                .message("Them khach hang thanh cong")
                .khachHang(response)
                .build();
    }

    @Override
    public PhanHoi update(Long id, KhachHangDTO dto) {
        KhachHang existing = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay khach hang"));
        if (dto.getName() != null) existing.setName(dto.getName());
        if (dto.getPhone() != null) existing.setPhone(dto.getPhone());
        if (dto.getEmail() != null) existing.setEmail(dto.getEmail());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());
        if (dto.getNotes() != null) existing.setNotes(dto.getNotes());
        khachHangRepository.save(existing);
        ThongKeKhachHangView stat = getStatsForIds(List.of(id)).get(id);
        return PhanHoi.builder()
                .status(200)
                .message("Cap nhat khach hang thanh cong")
                .khachHang(mapToDto(existing, stat))
                .build();
    }

    @Override
    public PhanHoi getAll() {
        List<KhachHang> list = khachHangRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<Long> ids = list.stream().map(KhachHang::getId).toList();
        Map<Long, ThongKeKhachHangView> stats = getStatsForIds(ids);
        List<KhachHangDTO> dtoList = list.stream()
                .map(khachHang -> mapToDto(khachHang, stats.get(khachHang.getId())))
                .toList();
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .khachHangs(dtoList)
                .build();
    }

    @Override
    public PhanHoi getById(Long id) {
        KhachHang entity = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay khach hang"));
        ThongKeKhachHangView stat = getStatsForIds(List.of(id)).get(id);
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .khachHang(mapToDto(entity, stat))
                .build();
    }

    @Override
    public PhanHoi detail(Long id) {
        KhachHang entity = khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay khach hang"));
        ThongKeKhachHangView stat = getStatsForIds(List.of(id)).get(id);
        List<GiaoDich> history = giaoDichRepository.findByCustomer_IdOrderByCreatedAtDesc(id);
        List<GiaoDichDTO> historyDtos = modelMapper.map(history, new TypeToken<List<GiaoDichDTO>>() {}.getType());
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .khachHang(mapToDto(entity, stat))
                .giaoDichs(historyDtos)
                .build();
    }

    @Override
    public PhanHoi delete(Long id) {
        khachHangRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay khach hang"));
        khachHangRepository.deleteById(id);
        return PhanHoi.builder().status(200).message("Xoa khach hang thanh cong").build();
    }

    private Map<Long, ThongKeKhachHangView> getStatsForIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        List<ThongKeKhachHangView> views = giaoDichRepository.thongKeTheoKhachHang(ids);
        Map<Long, ThongKeKhachHangView> map = new HashMap<>();
        for (ThongKeKhachHangView view : views) {
            map.put(view.getCustomerId(), view);
        }
        return map;
    }

    private KhachHangDTO mapToDto(KhachHang entity, ThongKeKhachHangView stat) {
        BigDecimal totalSpent = stat != null && stat.getTotalSpent() != null ? stat.getTotalSpent() : BigDecimal.ZERO;
        BigDecimal outstanding = stat != null && stat.getOutstanding() != null ? stat.getOutstanding() : BigDecimal.ZERO;
        Long totalOrders = stat != null && stat.getTotalOrders() != null ? stat.getTotalOrders() : 0L;
        return KhachHangDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .notes(entity.getNotes())
                .tongChiTieu(totalSpent)
                .congNo(outstanding)
                .tongGiaoDich(totalOrders)
                .build();
    }
    private void ghiNhatKy(String action, String message, Object oldData, Object newData, Long targetId) {
        nhatKyThayDoiService.ghi("KhachHang", action, message, "KhachHang", targetId, oldData, newData);
    }
}



