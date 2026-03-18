package com.github.deniskoriavets.smartexpensetracker.service;

import com.github.deniskoriavets.smartexpensetracker.dto.account.AccountResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.CategoryResponseDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.CreateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.UpdateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import com.github.deniskoriavets.smartexpensetracker.mapper.CategoryMapper;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(UUID.randomUUID())
                .email("test@email.com")
                .firstName("Test")
                .lastName("Test")
                .role(Role.USER)
                .passwordHash("password")
                .createdAt(LocalDateTime.now())
                .build();

        testCategory = Category.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Food")
                .type(CategoryType.EXPENSE)
                .build();
    }

    @Test
    @DisplayName("Should successfully create and return a new category")
    void createCategoryShouldReturnCreatedCategory() {
        //Arrange
        mockSecurityContext(testUser.getEmail());
        CreateCategoryDto createCategoryDto = new CreateCategoryDto("Test", CategoryType.EXPENSE);
        CategoryResponseDto expectedDto = new CategoryResponseDto(UUID.randomUUID(), "Test", CategoryType.EXPENSE);
        when(categoryMapper.toEntity(any())).thenReturn(testCategory);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryMapper.toResponseDto(any())).thenReturn(expectedDto);

        //Act
        var actualDto = categoryService.createCategory(createCategoryDto);

        //Assert
        assertNotNull(actualDto);
        assertEquals(expectedDto.id(), actualDto.id());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("Should return all categories belonging to the current user")
    void getAllCategoriesShouldReturnListOfCategories() {
        // Arrange
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        Category testCategory2 = Category.builder()
                .id(UUID.randomUUID())
                .user(testUser)
                .name("Test")
                .type(CategoryType.EXPENSE)
                .build();
        when(categoryRepository.findAllByUserId(testUser.getId())).thenReturn(List.of(testCategory, testCategory2));
        var expectedDto = new CategoryResponseDto(UUID.randomUUID(), "Test", CategoryType.EXPENSE);
        when(categoryMapper.toResponseDto(any())).thenReturn(expectedDto);

        //Act
        var actualDto = categoryService.getAllCategories();

        //Assert
        assertNotNull(actualDto);
        assertEquals(2, actualDto.size());
    }

    @Test
    @DisplayName("Should return category by id when user is owner")
    void getCategoryByIdShouldReturnCategoryWhenUserIsOwner() {
        // Arrange
        mockSecurityContext(testUser.getEmail());
        var expectedDto = new CategoryResponseDto(UUID.randomUUID(), "Test", CategoryType.EXPENSE);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponseDto(any())).thenReturn(expectedDto);

        //Act
        var actualDto = categoryService.getCategoryById(testCategory.getId());

        //Assert
        assertNotNull(actualDto);
        assertEquals(expectedDto.id(), actualDto.id());
        verify(categoryRepository).findById(testCategory.getId());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when user is not owner")
    void getCategoryByIdShouldThrowExceptionWhenUserIsNotOwner() {
        // Arrange
        User maliciousUser = User.builder()
                .id(UUID.randomUUID())
                .email("hacker@email.com")
                .build();

        mockSecurityContext(maliciousUser.getEmail());
        when(userRepository.findByEmail(maliciousUser.getEmail())).thenReturn(Optional.of(maliciousUser));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> {
            categoryService.getCategoryById(testCategory.getId());
        });
        verify(categoryMapper, never()).toResponseDto(any());
    }

    @Test
    @DisplayName("Should update category details when user is owner")
    void updateCategoryShouldReturnUpdatedCategoryWhenUserIsOwner() {
        //Arrange
        mockSecurityContext(testUser.getEmail());
        UpdateCategoryDto updateCategoryDto = new UpdateCategoryDto("Updated Name", CategoryType.EXPENSE);
        CategoryResponseDto expectedDto = new CategoryResponseDto(UUID.randomUUID(), "Updated Name", CategoryType.EXPENSE);
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponseDto(any())).thenReturn(expectedDto);

        //Act
        CategoryResponseDto categoryResponseDto = categoryService.updateCategory(testCategory.getId(), updateCategoryDto);


        //Assert
        assertNotNull(categoryResponseDto);
        assertEquals("Updated Name", categoryResponseDto.name());

        verify(categoryMapper).updateCategory(testCategory, updateCategoryDto);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    @DisplayName("Should delete category when user is owner")
    void deleteCategoryShouldRemoveCategoryWhenUserIsOwner(){
        //Arrange
        mockSecurityContext(testUser.getEmail());
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        when(categoryRepository.findById(testCategory.getId())).thenReturn(Optional.of(testCategory));

        //Act
        categoryService.deleteCategory(testCategory.getId());

        //Assert
        verify(categoryRepository).delete(testCategory);
    }

    private void mockSecurityContext(String email) {
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(email);

        SecurityContextHolder.setContext(securityContext);
    }
}