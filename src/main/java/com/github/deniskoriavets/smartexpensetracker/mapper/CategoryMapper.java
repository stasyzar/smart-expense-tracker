package com.github.deniskoriavets.smartexpensetracker.mapper;

import com.github.deniskoriavets.smartexpensetracker.dto.category.CategoryResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.CreateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.UpdateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    Category toEntity(CreateCategoryDto createCategoryDto);

    CategoryResponseDto toResponseDto(Category category);

    void updateCategory(@MappingTarget Category category, UpdateCategoryDto updateCategoryDto);
}
