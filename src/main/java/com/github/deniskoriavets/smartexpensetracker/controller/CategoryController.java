package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.category.CategoryResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.CreateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.UpdateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Управління категоріями")
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Отримати всі категорії", description = "Повертає список всіх категорій (і витрати, і доходи) поточного користувача")    public ResponseEntity<List<CategoryResponseDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @PostMapping
    @Operation(summary = "Створити категорію", description = "Додає нову категорію для фіксації транзакцій")
    public ResponseEntity<CategoryResponseDto> createCategory(@RequestBody @Valid CreateCategoryDto createCategoryDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(createCategoryDto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Отримати категорію за ID", description = "Повертає інформацію про конкретну категорію")
    public ResponseEntity<CategoryResponseDto> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Оновити категорію", description = "Змінює назву або тип існуючої категорії")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable UUID id, @RequestBody @Valid UpdateCategoryDto updateCategoryDto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, updateCategoryDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Видалити категорію", description = "Видаляє категорію (за умови, що до неї не прив'язані транзакції)")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
