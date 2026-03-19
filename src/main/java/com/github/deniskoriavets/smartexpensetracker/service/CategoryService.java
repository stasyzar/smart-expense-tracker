package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.category.CategoryResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.CreateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.UpdateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.mapper.CategoryMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final CategoryMapper categoryMapper;

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public CategoryResponseDto createCategory(CreateCategoryDto createCategoryDto) {
        var user = getCurrentUser();
        var category = categoryMapper.toEntity(createCategoryDto);
        category.setUser(user);
        categoryRepository.save(category);
        return categoryMapper.toResponseDto(category);
    }

    public List<CategoryResponseDto> getAllCategories() {
        var user = getCurrentUser();
        return categoryRepository.findAllByUserId(user.getId()).stream().map(categoryMapper::toResponseDto).toList();
    }

    public CategoryResponseDto getCategoryById(UUID id) {
        var user = getCurrentUser();
        var category = categoryRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!category.getUser().getId().equals(user.getId())) {
            throw new EntityNotFoundException();
        }
        return categoryMapper.toResponseDto(category);
    }

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public CategoryResponseDto updateCategory(UUID id, UpdateCategoryDto updateCategoryDto) {
        var user = getCurrentUser();
        var category = categoryRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!category.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();
        categoryMapper.updateCategory(category, updateCategoryDto);
        categoryRepository.save(category);
        return categoryMapper.toResponseDto(category);
    }

    @CacheEvict(value = {"analyticsSummary", "categoryAnalytics", "monthlyStats", "budgetStatus"}, allEntries = true)
    public void deleteCategory(UUID id) {
        var user = getCurrentUser();
        var category = categoryRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        if (!category.getUser().getId().equals(user.getId()))
            throw new EntityNotFoundException();
        categoryRepository.delete(category);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }
}