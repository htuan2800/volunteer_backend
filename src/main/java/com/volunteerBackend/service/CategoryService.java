package com.volunteerBackend.service;

import java.util.List;

import com.volunteerBackend.model.Category;

public interface CategoryService {
    public boolean createCategory(Category category) throws Exception;

    public Category getCategoryById(Long id);

    public List<Category> getAllCategories();

    public List<Category> getAllCategoryByStatus();

    public boolean updateCategory(Category category, Long id) throws Exception;

    public boolean deleteCategory(Long id) throws Exception;
}
