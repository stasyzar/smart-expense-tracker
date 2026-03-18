package com.github.deniskoriavets.smartexpensetracker.controller;

import com.github.deniskoriavets.smartexpensetracker.dto.category.CreateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.dto.category.UpdateCategoryDto;
import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import com.github.deniskoriavets.smartexpensetracker.repository.CategoryRepository;
import com.github.deniskoriavets.smartexpensetracker.repository.UserRepository;
import com.github.deniskoriavets.smartexpensetracker.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JwtService jwtService;

    private User testUser;
    private Category testCategory;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .email("test@email.com")
                .firstName("Test")
                .lastName("User")
                .passwordHash("hashedpassword")
                .role(Role.USER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(testUser);

        jwtToken = jwtService.generateAccessToken(testUser);

        testCategory = Category.builder()
                .user(testUser)
                .name("Test Category")
                .type(CategoryType.EXPENSE)
                .build();
        categoryRepository.save(testCategory);
    }

    @Test
    @DisplayName("Should return 200 OK and list of user Categories")
    void getAllCategoriesShouldReturn200AndList() throws Exception {
        mockMvc.perform(get("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Category"))
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 201 Created and the new Category")
    void createCategoryShouldReturn201() throws Exception {
        var createRequest = new CreateCategoryDto("New Category", CategoryType.EXPENSE);

        mockMvc.perform(post("/api/categories")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("New Category"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 200 OK and Category by id")
    void getCategoryByIdShouldReturn200AndCategoryById() throws Exception {
        mockMvc.perform(get("/api/categories/{id}", testCategory.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Category"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 200 OK and updated Category")
    void updateCategoryShouldReturn200AndCategoryUpdated() throws Exception {
        var updateDto = new UpdateCategoryDto("Updated Category", CategoryType.EXPENSE);

        mockMvc.perform(put("/api/categories/{id}", testCategory.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Category"))
                .andExpect(jsonPath("$.type").value("EXPENSE"));
    }

    @Test
    @DisplayName("Should return 204 No Content on delete")
    void deleteCategoryShouldReturn204NoContentOnDelete() throws Exception {
        mockMvc.perform(delete("/api/categories/{id}", testCategory.getId())
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }
}