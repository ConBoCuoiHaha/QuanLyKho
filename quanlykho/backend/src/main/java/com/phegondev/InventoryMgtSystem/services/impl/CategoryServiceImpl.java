package com.phegondev.InventoryMgtSystem.services.impl;


import com.phegondev.InventoryMgtSystem.dtos.CategoryDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.DanhMuc;
import com.phegondev.InventoryMgtSystem.repositories.DanhMucRepository;
import com.phegondev.InventoryMgtSystem.services.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    private final DanhMucRepository danhMucRepository;
    private final ModelMapper modelMapper;


    @Override
    public PhanHoi createCategory(CategoryDTO categoryDTO) {

        DanhMuc categoryToSave = modelMapper.map(categoryDTO, DanhMuc.class);

        danhMucRepository.save(categoryToSave);

        return PhanHoi.builder()
                .status(200)
                .message("Tạo danh mục thành công")
                .build();

    }

    @Override
    public PhanHoi getAllCategories() {
        List<DanhMuc> categories = danhMucRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        categories.forEach(category -> category.setSanPhams(null));

        List<CategoryDTO> categoryDTOList = modelMapper.map(categories, new TypeToken<List<CategoryDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .categories(categoryDTOList)
                .build();
    }

    @Override
    public PhanHoi getCategoryById(Long id) {

        DanhMuc category = danhMucRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        CategoryDTO categoryDTO = modelMapper.map(category, CategoryDTO.class);

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .category(categoryDTO)
                .build();
    }

    @Override
    public PhanHoi updateCategory(Long id, CategoryDTO categoryDTO) {

        DanhMuc existingCategory = danhMucRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        existingCategory.setName(categoryDTO.getName());

        danhMucRepository.save(existingCategory);

        return PhanHoi.builder()
                .status(200)
                .message("Cập nhật danh mục thành công")
                .build();

    }

    @Override
    public PhanHoi deleteCategory(Long id) {

        DanhMuc category = danhMucRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        if (category.getSanPhams() != null && !category.getSanPhams().isEmpty()) {
            throw new NotFoundException("Không thể xóa danh mục vì còn sản phẩm");
        }

        danhMucRepository.deleteById(id);

        return PhanHoi.builder()
                .status(200)
                .message("Xóa danh mục thành công")
                .build();
    }
}
