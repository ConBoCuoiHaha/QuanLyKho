package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.CategoryDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;

public interface CategoryService {

    PhanHoi createCategory(CategoryDTO categoryDTO);

    PhanHoi getAllCategories();

    PhanHoi getCategoryById(Long id);

    PhanHoi updateCategory(Long id, CategoryDTO categoryDTO);

    PhanHoi deleteCategory(Long id);
}
