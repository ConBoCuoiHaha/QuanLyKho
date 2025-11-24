package com.phegondev.InventoryMgtSystem.services.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phegondev.InventoryMgtSystem.dtos.NhatKyDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.UserDTO;
import com.phegondev.InventoryMgtSystem.models.NhatKyThayDoi;
import com.phegondev.InventoryMgtSystem.models.User;
import com.phegondev.InventoryMgtSystem.repositories.NhatKyThayDoiRepository;
import com.phegondev.InventoryMgtSystem.services.NhatKyThayDoiService;
import com.phegondev.InventoryMgtSystem.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class NhatKyThayDoiServiceImpl implements NhatKyThayDoiService {

    private final NhatKyThayDoiRepository nhatKyThayDoiRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public void ghi(String module,
                    String hanhDong,
                    String moTa,
                    String doiTuongLoai,
                    Long doiTuongId,
                    Object duLieuCu,
                    Object duLieuMoi) {
        try {
            User currentUser;
            try {
                currentUser = userService.getCurrentLoggedInUser();
            } catch (Exception ex) {
                currentUser = null;
            }

            NhatKyThayDoi logEntity = NhatKyThayDoi.builder()
                    .module(module)
                    .hanhDong(hanhDong)
                    .moTa(moTa)
                    .doiTuongLoai(doiTuongLoai)
                    .doiTuongId(doiTuongId)
                    .duLieuCu(convertToJson(duLieuCu))
                    .duLieuMoi(convertToJson(duLieuMoi))
                    .nguoiThucHien(currentUser)
                    .build();
            nhatKyThayDoiRepository.save(logEntity);
        } catch (Exception ex) {
            log.warn("Khong the ghi nhat ky thay doi: {}", ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PhanHoi timKiem(int page,
                           int size,
                           String module,
                           String hanhDong,
                           Long userId,
                           String doiTuongLoai,
                           Long doiTuongId,
                           LocalDate tuNgay,
                           LocalDate denNgay) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Specification<NhatKyThayDoi> spec = (root, query, cb) -> cb.conjunction();

        if (module != null && !module.isBlank()) {
            Specification<NhatKyThayDoi> moduleSpec = (root, query, cb) ->
                    cb.equal(cb.lower(root.get("module")), module.toLowerCase(Locale.ROOT));
            spec = spec.and(moduleSpec);
        }

        if (hanhDong != null && !hanhDong.isBlank()) {
            Specification<NhatKyThayDoi> actionSpec = (root, query, cb) ->
                    cb.equal(cb.lower(root.get("hanhDong")), hanhDong.toLowerCase(Locale.ROOT));
            spec = spec.and(actionSpec);
        }

        if (userId != null) {
            Specification<NhatKyThayDoi> userSpec = (root, query, cb) ->
                    cb.equal(root.get("nguoiThucHien").get("id"), userId);
            spec = spec.and(userSpec);
        }

        if (doiTuongLoai != null && !doiTuongLoai.isBlank()) {
            Specification<NhatKyThayDoi> targetTypeSpec = (root, query, cb) ->
                    cb.equal(cb.lower(root.get("doiTuongLoai")), doiTuongLoai.toLowerCase(Locale.ROOT));
            spec = spec.and(targetTypeSpec);
        }

        if (doiTuongId != null) {
            Specification<NhatKyThayDoi> targetIdSpec = (root, query, cb) ->
                    cb.equal(root.get("doiTuongId"), doiTuongId);
            spec = spec.and(targetIdSpec);
        }

        if (tuNgay != null) {
            Specification<NhatKyThayDoi> fromSpec = (root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), tuNgay.atStartOfDay());
            spec = spec.and(fromSpec);
        }

        if (denNgay != null) {
            Specification<NhatKyThayDoi> toSpec = (root, query, cb) ->
                    cb.lessThanOrEqualTo(root.get("createdAt"), denNgay.atTime(23, 59, 59));
            spec = spec.and(toSpec);
        }

        Page<NhatKyThayDoi> pageData = nhatKyThayDoiRepository.findAll(spec, pageable);
        List<NhatKyDTO> dtos = pageData.getContent().stream()
                .map(this::mapToDto)
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .nhatKys(dtos)
                .totalPages(pageData.getTotalPages())
                .totalElements(pageData.getTotalElements())
                .build();
    }

    private String convertToJson(Object object) throws JsonProcessingException {
        if (object == null) {
            return null;
        }
        if (object instanceof String str) {
            return str;
        }
        return objectMapper.writeValueAsString(object);
    }

    private NhatKyDTO mapToDto(NhatKyThayDoi entity) {
        NhatKyDTO dto = modelMapper.map(entity, NhatKyDTO.class);
        if (entity.getNguoiThucHien() != null) {
            dto.setNguoiThucHien(modelMapper.map(entity.getNguoiThucHien(), UserDTO.class));
        }
        return dto;
    }
}
