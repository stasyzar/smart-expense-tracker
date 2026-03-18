package com.github.deniskoriavets.smartexpensetracker.dto.category;

import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;

import java.util.UUID;

public record CategoryResponseDto(UUID id, String name, CategoryType type) {
}
