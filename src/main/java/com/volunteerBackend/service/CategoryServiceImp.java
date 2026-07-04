package com.volunteerBackend.service;

import java.util.List;


import org.springframework.stereotype.Service;

import com.volunteerBackend.model.Category;
import com.volunteerBackend.repository.CategoryRepository;

@Service
public class CategoryServiceImp implements CategoryService {
    
    private CategoryRepository categoryRepository;
    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getAllCategoryByStatus() {
        return categoryRepository.findByIsDeleted(false);
    }

    @Override
    public boolean createCategory(Category category) throws Exception {
        if(categoryRepository.existsByName(category.getName())){
            throw new Exception("Category already exists");
        }
        categoryRepository.save(category);
        return true;
    }

    @Override
    public boolean updateCategory(Category category, Long id) throws Exception {
        Category oldCategory = categoryRepository.findById(id).orElse(null);
        if(oldCategory == null){
            throw new Exception("Category not found");
        }
        if(categoryRepository.existsByNameAndIdNot(category.getName(), oldCategory.getId())){
            throw new Exception("Category already exists");
        }
        oldCategory.setDescription(category.getDescription());
        oldCategory.setName(category.getName());
        oldCategory.setSlug(category.getSlug());
        categoryRepository.save(oldCategory);
        return true;
    }

    @Override
    public boolean deleteCategory(Long id) throws Exception {
        Category category = categoryRepository.findById(id).orElse(null);
        if(category == null){
            throw new Exception("Category not found");
        }
        category.setIsDeleted(true);
        categoryRepository.save(category);
        return true;
    }

    @Override
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }
}
