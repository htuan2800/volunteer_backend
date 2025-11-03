package com.volunteerBackend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.volunteerBackend.DTO.CategoryDTO;
import com.volunteerBackend.mapper.CategoryMapper;
import com.volunteerBackend.model.Category;

import com.volunteerBackend.service.CategoryService;

@RestController
@RequestMapping("/api")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryMapper categoryMapper;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategoriesForUser(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<Category> categories = categoryService.getAllCategoryByStatus();
        List<CategoryDTO> CategoryDTOs = categoryMapper.toDTOList(categories);
        return new ResponseEntity<>(CategoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/admin/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories(
            @RequestHeader(value = "Authorization", required = false) String jwt) {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDTO> CategoryDTOs = categoryMapper.toDTOList(categories);
        return new ResponseEntity<>(CategoryDTOs, HttpStatus.OK);
    }

    @GetMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategory(@PathVariable Long id) {
        Category category = categoryService.getCategoryById(id);
        CategoryDTO CategoryDTO = categoryMapper.toDTO(category);
        return new ResponseEntity<>(CategoryDTO, HttpStatus.OK);
    }

    @PutMapping("/admin/categories/update_category/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Long id, @RequestBody Category category) throws Exception {
        boolean isSuccess = categoryService.updateCategory(category, id);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @PutMapping("/admin/categories/delete_category/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) throws Exception {
        boolean isSuccess = categoryService.deleteCategory(id);
        return new ResponseEntity<>(isSuccess, HttpStatus.OK);
    }

    @PostMapping("/admin/categories/add_category")
    public ResponseEntity<?> addCategory(@RequestBody Category category) throws Exception {
        boolean isSuccess = categoryService.createCategory(category);
        return new ResponseEntity<>(isSuccess, HttpStatus.CREATED);
    }


}
