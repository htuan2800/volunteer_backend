package com.volunteerBackend.mapper;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.volunteerBackend.DTO.CategoryDTO;
import com.volunteerBackend.model.Category;



@Component
public class CategoryMapper {
    public CategoryDTO toDTO(Category category) {
        if (category == null)
            return null;

        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setSlug(category.getSlug());
        dto.setDescription(category.getDescription());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setDeleted(category.getIsDeleted());
        return dto;
    }

    public List<CategoryDTO> toDTOList(List<Category> categories) {
        if (categories == null)
            return Collections.emptyList();

        return categories.stream()
                .map(category -> toDTO(category))
                .collect(Collectors.toList());
    }
}
