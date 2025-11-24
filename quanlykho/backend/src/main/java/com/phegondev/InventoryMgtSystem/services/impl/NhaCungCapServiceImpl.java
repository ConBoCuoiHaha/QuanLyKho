package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.NhaCungCap;
import com.phegondev.InventoryMgtSystem.repositories.NhaCungCapRepository;
import com.phegondev.InventoryMgtSystem.services.NhaCungCapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NhaCungCapServiceImpl implements NhaCungCapService {

    private final NhaCungCapRepository nhaCungCapRepository;
    private final ModelMapper modelMapper;

    @Override
    public PhanHoi themNhaCungCap(NhaCungCapDTO nhaCungCapDTO) {
        NhaCungCap entity = modelMapper.map(nhaCungCapDTO, NhaCungCap.class);
        nhaCungCapRepository.save(entity);
        return PhanHoi.builder()
                .status(200)
                .message("Them nha cung cap thanh cong")
                .build();
    }

    @Override
    public PhanHoi capNhatNhaCungCap(Long id, NhaCungCapDTO nhaCungCapDTO) {
        NhaCungCap existing = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));

        if (nhaCungCapDTO.getName() != null) existing.setName(nhaCungCapDTO.getName());
        if (nhaCungCapDTO.getContactInfo() != null) existing.setContactInfo(nhaCungCapDTO.getContactInfo());
        if (nhaCungCapDTO.getAddress() != null) existing.setAddress(nhaCungCapDTO.getAddress());

        nhaCungCapRepository.save(existing);

        return PhanHoi.builder()
                .status(200)
                .message("Cap nhat nha cung cap thanh cong")
                .build();
    }

    @Override
    public PhanHoi layTatCaNhaCungCap() {
        List<NhaCungCap> danhSach = nhaCungCapRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<NhaCungCapDTO> dtoList = modelMapper.map(danhSach, new TypeToken<List<NhaCungCapDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .nhaCungCaps(dtoList)
                .build();
    }

    @Override
    public PhanHoi layNhaCungCapTheoId(Long id) {
        NhaCungCap nhaCungCap = nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));

        NhaCungCapDTO dto = modelMapper.map(nhaCungCap, NhaCungCapDTO.class);

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .nhaCungCap(dto)
                .build();
    }

    @Override
    public PhanHoi xoaNhaCungCap(Long id) {
        nhaCungCapRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Khong tim thay nha cung cap"));

        nhaCungCapRepository.deleteById(id);

        return PhanHoi.builder()
                .status(200)
                .message("Xoa nha cung cap thanh cong")
                .build();
    }
}
