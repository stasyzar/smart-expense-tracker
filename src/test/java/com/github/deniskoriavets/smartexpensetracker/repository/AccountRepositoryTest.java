package com.github.deniskoriavets.smartexpensetracker.repository;

import com.github.deniskoriavets.smartexpensetracker.entity.*;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.AccountType;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Currency;
import com.github.deniskoriavets.smartexpensetracker.entity.enums.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
public class AccountRepositoryTest {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find accounts only belonging to specific user")
    void findByUserIdTest() {
        // Arrange
        var user1 = userRepository.save(User.builder()
                .email("user1@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("One").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());

        var user2 = userRepository.save(User.builder()
                .email("user2@ukma.edu.ua").passwordHash("hash")
                .firstName("User").lastName("Two").role(Role.USER)
                .createdAt(LocalDateTime.now()).isActive(true).build());

        var account1 = accountRepository.save(Account.builder()
                .user(user1).name("Wallet 1").type(AccountType.CASH)
                .currency(Currency.UAH).createdAt(LocalDateTime.now()).build());

        var account2 = accountRepository.save(Account.builder()
                .user(user2).name("Wallet 2").type(AccountType.CARD)
                .currency(Currency.EUR).createdAt(LocalDateTime.now()).build());

        // Act
        var foundForUser1 = accountRepository.findByUserId(user1.getId());
        var foundForUser2 = accountRepository.findByUserId(user2.getId());

        // Assert
        assertThat(foundForUser1).hasSize(1);
        assertThat(foundForUser1.getFirst().getName()).isEqualTo("Wallet 1");
        assertThat(foundForUser1.getFirst().getUser().getId()).isEqualTo(user1.getId());

        assertThat(foundForUser2).hasSize(1);
        assertThat(foundForUser2.getFirst().getName()).isEqualTo("Wallet 2");
    }
}
