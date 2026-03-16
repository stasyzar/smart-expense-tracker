package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.Category;
import com.github.deniskoriavets.smartexpensetracker.entity.CategoryType;
import com.github.deniskoriavets.smartexpensetracker.entity.Role;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class CategoryRepositoryTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return only categories of the specified type for a given user")
    void findAllByUserIdAndTypeTest() {
        //Arrange
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());

        var category1 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.EXPENSE)
                .name("Expense")
                .build());
        var category2 = categoryRepository.save(Category.builder()
                .user(user1)
                .type(CategoryType.INCOME)
                .name("Income")
                .build());

        //Act
        var result = categoryRepository.findAllByUserIdAndType(user1.getId(), CategoryType.EXPENSE);

        //Assert
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getName()).isEqualTo("Expense");
        assertThat(result.getFirst().getUser().getId()).isEqualTo(user1.getId());
    }
}
