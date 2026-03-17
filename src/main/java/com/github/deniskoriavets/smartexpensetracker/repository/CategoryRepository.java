package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findAllByUserIdAndType(UUID userId, CategoryType type);
}
