package com.volunteerBackend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;


import com.volunteerBackend.model.Category;


public interface CategoryRepository extends JpaRepository<Category, Long>  {
    Category findByName(String name);
    Optional<Category> findBySlug(String slug);
    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    List<Category> findByIsDeleted (boolean deleted);
}
