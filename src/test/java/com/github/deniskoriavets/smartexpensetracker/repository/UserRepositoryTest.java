package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.Role;
import com.github.deniskoriavets.smartexpensetracker.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;


import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should save user and find him by email")
    void shouldSaveAndFindByEmail() {
        // Arrange
        User user = User.builder()
                .email("test@kma.edu.ua")
                .passwordHash("hashed_password")
                .firstName("Denys")
                .lastName("Koriavets")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // Act
        userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test@kma.edu.ua");

        // Assert
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test@kma.edu.ua");
        assertThat(foundUser.get().getId()).isNotNull();
    }

    @Test
    @DisplayName("Should return empty optional when email not exists")
    void shouldReturnEmptyOptionalWhenEmailNotExists() {
        // Arrange
        User user = User.builder()
                .email("test@kma.edu.ua")
                .passwordHash("hashed_password")
                .firstName("Denys")
                .lastName("Koriavets")
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // Act
        userRepository.save(user);
        Optional<User> foundUser = userRepository.findByEmail("test1@kma.edu.ua");

        //Assert
        assertThat(foundUser).isNotPresent();
    }
}