package com.github.deniskoriavets.smartexpensetracker.dto.category;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateCategoryDto(@NotBlank String name, @NotNull CategoryType type) {
}
